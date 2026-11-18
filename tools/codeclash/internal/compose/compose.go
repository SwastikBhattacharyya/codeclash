package compose

import (
	"context"
	"fmt"
	"io"
	"strings"

	"codeclash/internal/execx"
	"codeclash/internal/repo"
)

const (
	DB       = "db"
	Redpanda = "redpanda"
	Mailpit  = "mailpit"
	Grafana  = "grafana"
	PgAdmin  = "pgadmin"
	KafkaUI  = "kafka-ui"
)

var Infra = []string{DB, Redpanda, Mailpit, Grafana, PgAdmin, KafkaUI}

type Compose struct {
	Repo *repo.Repo
	Env  map[string]string
}

func Open(r *repo.Repo) (*Compose, error) {
	env, err := r.DotEnv()
	if err != nil {
		return nil, err
	}
	return &Compose{Repo: r, Env: env}, nil
}

func (c *Compose) Cmd(args ...string) *execx.Cmd {
	return execx.Command("docker", append([]string{"compose"}, args...)...).Dir(c.Repo.Root)
}

func (c *Compose) Exec(service string, args ...string) *execx.Cmd {
	return c.Cmd(append([]string{"exec", "-T", service}, args...)...)
}

func (c *Compose) Value(key string) (string, error) {
	value := c.Env[key]
	if value == "" {
		return "", fmt.Errorf("%s is not set in .env", key)
	}
	return value, nil
}

func (c *Compose) URL(portKey string) (string, error) {
	port, err := c.Value(portKey)
	if err != nil {
		return "", err
	}
	return "http://localhost:" + port, nil
}

func (c *Compose) Running(ctx context.Context) (map[string]bool, error) {
	out, err := c.Cmd("ps", "--status", "running", "--services").Output(ctx)
	if err != nil {
		return nil, err
	}
	running := map[string]bool{}
	for _, service := range strings.Fields(out) {
		running[service] = true
	}
	return running, nil
}

func (c *Compose) Up(ctx context.Context, w io.Writer) error {
	return c.Cmd(append([]string{"up", "--detach", "--wait"}, Infra...)...).Run(ctx, w)
}

func (c *Compose) Down(ctx context.Context, w io.Writer) error {
	return c.Cmd("down").Run(ctx, w)
}

func (c *Compose) Status(ctx context.Context) (string, error) {
	return c.Cmd("ps", "--all", "--format", "table {{.Service}}\t{{.State}}\t{{.Status}}\t{{.Ports}}").Output(ctx)
}
