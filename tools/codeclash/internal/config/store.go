package config

import (
	"errors"
	"fmt"
	"io/fs"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"github.com/pelletier/go-toml/v2"
)

const (
	DirMode       = 0o700
	FileMode      = 0o600
	profileSuffix = ".toml"
	activeFile    = "active"
	profilesDir   = "profiles"
)

var ErrNoProfile = errors.New("no profile configured yet")

type Store struct {
	dir string
}

func OpenStore() (*Store, error) {
	base := os.Getenv("XDG_CONFIG_HOME")
	if base == "" {
		home, err := os.UserHomeDir()
		if err != nil {
			return nil, err
		}
		base = filepath.Join(home, ".config")
	}
	return &Store{dir: filepath.Join(base, "codeclash")}, nil
}

func (s *Store) Dir() string {
	return s.dir
}

func (s *Store) path(name string) string {
	return filepath.Join(s.dir, profilesDir, name+profileSuffix)
}

func (s *Store) List() ([]string, error) {
	entries, err := os.ReadDir(filepath.Join(s.dir, profilesDir))
	if errors.Is(err, fs.ErrNotExist) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	var names []string
	for _, entry := range entries {
		if !entry.IsDir() && strings.HasSuffix(entry.Name(), profileSuffix) {
			names = append(names, strings.TrimSuffix(entry.Name(), profileSuffix))
		}
	}
	sort.Strings(names)
	return names, nil
}

func (s *Store) Exists(name string) bool {
	if ValidateProfileName(name) != nil {
		return false
	}
	_, err := os.Stat(s.path(name))
	return err == nil
}

func (s *Store) Load(name string) (*Profile, error) {
	if err := ValidateProfileName(name); err != nil {
		return nil, fmt.Errorf("profile %q: %w", name, err)
	}
	path := s.path(name)
	info, err := os.Stat(path)
	if errors.Is(err, fs.ErrNotExist) {
		return nil, fmt.Errorf("profile %s does not exist", name)
	}
	if err != nil {
		return nil, err
	}
	if info.Mode().Perm()&^FileMode != 0 {
		if err := os.Chmod(path, FileMode); err != nil {
			return nil, err
		}
	}
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}
	profile := &Profile{}
	if err := toml.Unmarshal(data, profile); err != nil {
		return nil, fmt.Errorf("%s: %w", path, err)
	}
	profile.Name = name
	if profile.K8s != nil && profile.K8s.Databases == nil {
		profile.K8s.Databases = map[string]*Database{}
	}
	return profile, nil
}

func (s *Store) Save(profile *Profile) error {
	if err := ValidateProfileName(profile.Name); err != nil {
		return err
	}
	dir := filepath.Dir(s.path(profile.Name))
	if err := os.MkdirAll(dir, DirMode); err != nil {
		return err
	}
	for _, d := range []string{s.dir, dir} {
		if err := os.Chmod(d, DirMode); err != nil {
			return err
		}
	}
	data, err := toml.Marshal(profile)
	if err != nil {
		return err
	}
	return s.writeAtomic(dir, profile.Name+profileSuffix, data)
}

func (s *Store) Active() (string, error) {
	data, err := os.ReadFile(filepath.Join(s.dir, activeFile))
	if errors.Is(err, fs.ErrNotExist) {
		names, listErr := s.List()
		if listErr != nil {
			return "", listErr
		}
		if len(names) == 0 {
			return "", ErrNoProfile
		}
		return names[0], nil
	}
	if err != nil {
		return "", err
	}
	name := strings.TrimSpace(string(data))
	if !s.Exists(name) {
		return "", fmt.Errorf("active profile %s does not exist", name)
	}
	return name, nil
}

func (s *Store) SetActive(name string) error {
	if !s.Exists(name) {
		return fmt.Errorf("profile %s does not exist", name)
	}
	return s.writeAtomic(s.dir, activeFile, []byte(name+"\n"))
}

func (s *Store) writeAtomic(dir string, name string, data []byte) error {
	if err := os.MkdirAll(dir, DirMode); err != nil {
		return err
	}
	tmp, err := os.CreateTemp(dir, "."+name+".*")
	if err != nil {
		return err
	}
	defer os.Remove(tmp.Name())
	if err := tmp.Chmod(FileMode); err != nil {
		tmp.Close()
		return err
	}
	if _, err := tmp.Write(data); err != nil {
		tmp.Close()
		return err
	}
	if err := tmp.Sync(); err != nil {
		tmp.Close()
		return err
	}
	if err := tmp.Close(); err != nil {
		return err
	}
	return os.Rename(tmp.Name(), filepath.Join(dir, name))
}
