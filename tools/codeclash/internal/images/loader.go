package images

import (
	"context"
	"io"
	"os"

	"codeclash/internal/execx"
	"codeclash/internal/kube"
)

type Loader interface {
	Describe() string
	Preloads() bool
	Has(ctx context.Context, image string) bool
	Load(ctx context.Context, w io.Writer, image string) error
}

type KindLoader struct {
	Cluster  kube.Kind
	Platform string
}

func (l KindLoader) Describe() string {
	return "kind " + l.Cluster.Name
}

func (l KindLoader) Preloads() bool {
	return true
}

func (l KindLoader) Has(ctx context.Context, image string) bool {
	return l.Cluster.HasImage(ctx, image)
}

func (l KindLoader) Load(ctx context.Context, w io.Writer, image string) error {
	return execx.Chain(ctx, w,
		execx.Command("docker", "save", "--platform", l.Platform, image),
		execx.Command("kind", "load", "image-archive", "--name", l.Cluster.Name, "/dev/stdin"))
}

type K3sLoader struct {
	Platform string
}

func (l K3sLoader) Describe() string {
	return "k3s containerd on this machine"
}

func (l K3sLoader) Preloads() bool {
	return false
}

func (l K3sLoader) Has(ctx context.Context, image string) bool {
	return false
}

func (l K3sLoader) command() *execx.Cmd {
	if os.Geteuid() == 0 {
		return execx.Command("k3s", "ctr", "images", "import", "-")
	}
	return execx.Command("sudo", "-n", "k3s", "ctr", "images", "import", "-")
}

func (l K3sLoader) Load(ctx context.Context, w io.Writer, image string) error {
	return execx.Chain(ctx, w, execx.Command("docker", "save", "--platform", l.Platform, image), l.command())
}

func NeedsSudo() bool {
	return os.Geteuid() != 0
}

type RegistryLoader struct {
	Registry string
}

func (l RegistryLoader) Describe() string {
	return "registry " + l.Registry
}

func (l RegistryLoader) Preloads() bool {
	return false
}

func (l RegistryLoader) Has(ctx context.Context, image string) bool {
	return false
}

func (l RegistryLoader) Load(ctx context.Context, w io.Writer, image string) error {
	return execx.Command("docker", "push", image).Run(ctx, w)
}
