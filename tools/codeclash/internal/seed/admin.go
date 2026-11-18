package seed

import (
	"context"
	"crypto/rand"
	"encoding/base64"
	"errors"
	"fmt"
	"io"
	"strings"
	"time"

	"golang.org/x/crypto/argon2"
	"golang.org/x/text/unicode/norm"

	"codeclash/internal/config"
	"codeclash/internal/pg"
	"codeclash/internal/ui"
)

const (
	SuperadminRole  = "superadmin"
	Argon2SaltSize  = 16
	Argon2HashSize  = 32
	Argon2Threads   = 1
	Argon2MemoryKiB = 19456
	Argon2Time      = 2
	RoleWait        = 3 * time.Minute
	RolePoll        = 3 * time.Second
)

const roleQuery = `select id from roles where name = :'role';
`

const authUpsert = `insert into users (name, username, email, password_hash, is_verified, role_id)
values (:'name', :'username', :'email', :'password_hash', true, :'role_id')
on conflict (username) do update set
  name          = excluded.name,
  email         = excluded.email,
  password_hash = excluded.password_hash,
  is_verified   = true,
  role_id       = excluded.role_id
returning id, username, created_at, updated_at;
`

const copyUpsert = `insert into users (id, username, created_at, updated_at)
values (:'id', :'username', :'created_at', :'updated_at')
on conflict (id) do update set
  username   = excluded.username,
  updated_at = excluded.updated_at;
`

func HashPassword(password string) (string, error) {
	salt := make([]byte, Argon2SaltSize)
	if _, err := rand.Read(salt); err != nil {
		return "", err
	}
	normalized := norm.NFKC.String(password)
	hash := argon2.IDKey([]byte(normalized), salt, Argon2Time, Argon2MemoryKiB, Argon2Threads, Argon2HashSize)
	return fmt.Sprintf("$argon2id$v=%d$m=%d,t=%d,p=%d$%s$%s", argon2.Version, Argon2MemoryKiB, Argon2Time, Argon2Threads,
		base64.RawStdEncoding.EncodeToString(salt), base64.RawStdEncoding.EncodeToString(hash)), nil
}

func Admin(ctx context.Context, console *ui.Console, postgres pg.Postgres, admin config.Admin, roleHint string) error {
	console.Section("Superadmin " + admin.Username)
	var roleID string
	if err := console.Run(ctx, "Wait for the "+SuperadminRole+" role", func(ctx context.Context, w io.Writer) error {
		deadline := time.Now().Add(RoleWait)
		for {
			out, err := postgres.Query(ctx, "auth", roleQuery, map[string]string{"role": SuperadminRole})
			if err != nil {
				return err
			}
			if roleID = strings.TrimSpace(out); roleID != "" {
				return nil
			}
			if time.Now().After(deadline) {
				return errors.New("role " + SuperadminRole + " not found: " + roleHint)
			}
			select {
			case <-ctx.Done():
				return ctx.Err()
			case <-time.After(RolePoll):
			}
		}
	}); err != nil {
		return err
	}
	hash, err := HashPassword(admin.Password)
	if err != nil {
		return console.Errorf("hashing the password: %v", err)
	}
	var id, username, createdAt, updatedAt string
	if err := console.Run(ctx, "Write "+admin.Username+" to the auth database", func(ctx context.Context, w io.Writer) error {
		out, err := postgres.Query(ctx, "auth", authUpsert, map[string]string{
			"name": admin.Name, "username": admin.Username, "email": admin.Email, "password_hash": hash, "role_id": roleID,
		})
		if err != nil {
			return err
		}
		fields := strings.Split(strings.TrimSpace(out), "|")
		if len(fields) != 4 || fields[0] == "" {
			return fmt.Errorf("unexpected auth database answer %q", out)
		}
		id, username, createdAt, updatedAt = fields[0], fields[1], fields[2], fields[3]
		return nil
	}); err != nil {
		return err
	}
	for _, database := range []string{"problem", "contest"} {
		if err := console.Run(ctx, "Copy "+username+" to the "+database+" database", func(ctx context.Context, w io.Writer) error {
			_, err := postgres.Query(ctx, database, copyUpsert, map[string]string{
				"id": id, "username": username, "created_at": createdAt, "updated_at": updatedAt,
			})
			return err
		}); err != nil {
			return err
		}
	}
	console.OK("%s (%s) can log in as %s", username, id, SuperadminRole)
	return nil
}
