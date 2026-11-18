package repo

import (
	"bufio"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"

	"codeclash/internal/execx"
)

const (
	RootEnv          = "CODECLASH_ROOT"
	NoCommitRevision = "uncommitted"
	DirtySuffix      = "-dirty"
	ShortSHALength   = 7
)

var gradleVersion = regexp.MustCompile(`(?m)^version\s*=\s*"([^"]+)"`)

type Repo struct {
	Root string
}

func Locate() (*Repo, error) {
	if root := os.Getenv(RootEnv); root != "" {
		return &Repo{Root: root}, nil
	}
	dir, err := os.Getwd()
	if err != nil {
		return nil, err
	}
	for {
		if isRoot(dir) {
			return &Repo{Root: dir}, nil
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			return nil, errors.New("run codeclash from inside the CodeClash repository")
		}
		dir = parent
	}
}

func isRoot(dir string) bool {
	for _, marker := range []string{"settings.gradle.kts", "k8s", "tools/codeclash"} {
		if _, err := os.Stat(filepath.Join(dir, marker)); err != nil {
			return false
		}
	}
	return true
}

func (r *Repo) Path(parts ...string) string {
	return filepath.Join(append([]string{r.Root}, parts...)...)
}

func (r *Repo) ServiceVersion(service string) (string, error) {
	path := r.Path("app", service, "build.gradle.kts")
	data, err := os.ReadFile(path)
	if err != nil {
		return "", err
	}
	match := gradleVersion.FindSubmatch(data)
	if match == nil {
		return "", fmt.Errorf("%s declares no version", path)
	}
	return string(match[1]), nil
}

func (r *Repo) WebVersion() (string, error) {
	path := r.Path("app", "web", "package.json")
	data, err := os.ReadFile(path)
	if err != nil {
		return "", err
	}
	var manifest struct {
		Version string `json:"version"`
	}
	if err := json.Unmarshal(data, &manifest); err != nil {
		return "", fmt.Errorf("%s: %w", path, err)
	}
	if manifest.Version == "" {
		return "", fmt.Errorf("%s declares no version", path)
	}
	return manifest.Version, nil
}

func (r *Repo) Revision(ctx context.Context) (string, error) {
	sha, err := execx.Command("git", "rev-parse", "--short="+strconv.Itoa(ShortSHALength), "HEAD").Dir(r.Root).Output(ctx)
	revision := strings.TrimSpace(sha)
	if err != nil || revision == "" {
		revision = NoCommitRevision
	}
	status, err := execx.Command("git", "status", "--porcelain").Dir(r.Root).Output(ctx)
	if err != nil || strings.TrimSpace(status) != "" {
		revision += DirtySuffix
	}
	return revision, nil
}

func (r *Repo) DotEnv() (map[string]string, error) {
	file, err := os.Open(r.Path(".env"))
	if err != nil {
		return nil, fmt.Errorf(".env not found in %s: %w", r.Root, err)
	}
	defer file.Close()
	values := map[string]string{}
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}
		key, value, found := strings.Cut(line, "=")
		if !found {
			continue
		}
		value = strings.TrimSpace(value)
		if len(value) >= 2 && (value[0] == '"' || value[0] == '\'') && value[len(value)-1] == value[0] {
			value = value[1 : len(value)-1]
		}
		values[strings.TrimSpace(strings.TrimPrefix(key, "export "))] = value
	}
	return values, scanner.Err()
}
