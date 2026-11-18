package images

import (
	"context"
	_ "embed"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"

	"codeclash/internal/execx"
	"codeclash/internal/repo"
)

const (
	Web            = "web"
	Runner         = "runner-service"
	SandboxImage   = "codeclash-runner:local"
	SandboxBundle  = "sandbox-bundle"
	Namespace      = "codeclash/"
	MaxTagLength   = 128
	BundleFileMode = 0o600
	PostgresImage  = "postgres:18.6-alpine"
	RedpandaImage  = "redpandadata/redpanda:v26.2.6"
	FlywayImage    = "flyway/flyway:12.4.0"
	DindImage      = "docker:29.9.3-dind"
	GrafanaImage   = "grafana/otel-lgtm:0.34.0"
	MailpitImage   = "axllent/mailpit:v1.31.6"
	PgAdminImage   = "dpage/pgadmin4:9.19.0"
	KafkaUIImage   = "ghcr.io/kafbat/kafka-ui:v1.5.0"
	bundleTarball  = "sandbox.tar"
	bundleDocument = "Dockerfile"
)

var Services = []string{"auth-service", "problem-service", "contest-service", "runner-service", "notification-service", "gateway-service", Web}

//go:embed sandbox-bundle.Dockerfile
var bundleDockerfile string

type Set struct {
	Registry string
	tags     map[string]string
}

func Resolve(ctx context.Context, r *repo.Repo, registry string) (*Set, error) {
	revision, err := r.Revision(ctx)
	if err != nil {
		return nil, err
	}
	set := &Set{Registry: strings.TrimSuffix(registry, "/"), tags: map[string]string{}}
	for _, service := range Services {
		var version string
		if service == Web {
			version, err = r.WebVersion()
		} else {
			version, err = r.ServiceVersion(service)
		}
		if err != nil {
			return nil, err
		}
		tag := version + "-" + revision
		if len(tag) > MaxTagLength || strings.ContainsFunc(tag, invalidTagRune) || tag[0] == '.' || tag[0] == '-' {
			return nil, fmt.Errorf("%s version %q does not make a valid image tag", service, version)
		}
		set.tags[service] = tag
	}
	set.tags[SandboxBundle] = set.tags[Runner]
	return set, nil
}

func invalidTagRune(r rune) bool {
	return !(r >= 'a' && r <= 'z' || r >= 'A' && r <= 'Z' || r >= '0' && r <= '9' || r == '_' || r == '.' || r == '-')
}

func (s *Set) Prefix() string {
	if s.Registry == "" {
		return Namespace
	}
	return s.Registry + "/" + Namespace
}

func (s *Set) Tag(service string) string {
	return s.tags[service]
}

func (s *Set) Ref(service string) string {
	return s.Prefix() + service + ":" + s.tags[service]
}

func (s *Set) Own() []string {
	refs := make([]string, 0, len(Services)+1)
	for _, service := range Services {
		refs = append(refs, s.Ref(service))
	}
	return append(refs, s.Ref(SandboxBundle))
}

func Present(ctx context.Context, image string) bool {
	_, err := execx.Command("docker", "image", "inspect", image).Output(ctx)
	return err == nil
}

func (s *Set) Missing(ctx context.Context) []string {
	var missing []string
	for _, ref := range s.Own() {
		if !Present(ctx, ref) {
			missing = append(missing, ref)
		}
	}
	return missing
}

func (s *Set) BuildService(ctx context.Context, w io.Writer, r *repo.Repo, service string) error {
	if service == Web {
		return execx.Command("docker", "buildx", "build", "--load", "--tag", s.Ref(service), r.Path("app", "web")).Run(ctx, w)
	}
	return execx.Command("docker", "buildx", "build", "--load", "--tag", s.Ref(service),
		"--file", r.Path("app", service, "Dockerfile"), r.Root).Run(ctx, w)
}

func BuildSandbox(ctx context.Context, w io.Writer, r *repo.Repo) error {
	return execx.Command("docker", "buildx", "build", "--load", "--tag", SandboxImage, r.Path("docker", "runner")).Run(ctx, w)
}

func (s *Set) BuildBundle(ctx context.Context, w io.Writer) error {
	dir, err := os.MkdirTemp("", "codeclash-bundle-")
	if err != nil {
		return err
	}
	defer os.RemoveAll(dir)
	if err := os.WriteFile(filepath.Join(dir, bundleDocument), []byte(bundleDockerfile), BundleFileMode); err != nil {
		return err
	}
	if err := execx.Command("docker", "save", "--output", filepath.Join(dir, bundleTarball), SandboxImage).Run(ctx, w); err != nil {
		return err
	}
	return execx.Command("docker", "buildx", "build", "--load", "--tag", s.Ref(SandboxBundle), dir).Run(ctx, w)
}

func Pull(ctx context.Context, w io.Writer, image string) error {
	if Present(ctx, image) {
		return nil
	}
	return execx.Command("docker", "pull", image).Run(ctx, w)
}

func Platform(ctx context.Context) (string, error) {
	out, err := execx.Command("docker", "version", "--format", "{{.Server.Os}}/{{.Server.Arch}}").Output(ctx)
	if err != nil {
		return "", err
	}
	return strings.TrimSpace(out), nil
}
