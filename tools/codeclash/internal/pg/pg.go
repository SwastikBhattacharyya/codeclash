package pg

import (
	"context"
	"fmt"
	"io"
	"sort"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/cluster"
	"codeclash/internal/compose"
	"codeclash/internal/config"
	"codeclash/internal/images"
	"codeclash/internal/kube"
)

const (
	HelperLifetimeSeconds = 900
	HelperReady           = 2 * time.Minute
	HelperCleanup         = 30 * time.Second
	HelperName            = "codeclash-psql"
	HelperRenewMargin     = 5 * time.Minute
	HelperRenewAfter      = HelperLifetimeSeconds*time.Second - HelperRenewMargin
)

type Postgres interface {
	Query(ctx context.Context, database string, script string, vars map[string]string) (string, error)
	Close()
}

func psqlArgs(vars map[string]string) []string {
	args := []string{"-v", "ON_ERROR_STOP=1", "-qtA", "-F", "|"}
	keys := make([]string, 0, len(vars))
	for key := range vars {
		keys = append(keys, key)
	}
	sort.Strings(keys)
	for _, key := range keys {
		args = append(args, "-v", key+"="+vars[key])
	}
	return args
}

type Compose struct {
	Compose *compose.Compose
	User    string
}

func NewCompose(c *compose.Compose) (*Compose, error) {
	user, err := c.Value("DB_USER")
	if err != nil {
		return nil, err
	}
	return &Compose{Compose: c, User: user}, nil
}

func (p *Compose) Query(ctx context.Context, database string, script string, vars map[string]string) (string, error) {
	args := append([]string{"psql", "-U", p.User, "-d", database}, psqlArgs(vars)...)
	return p.Compose.Exec(compose.DB, args...).StdinString(script).Output(ctx)
}

func (p *Compose) Close() {}

type Helper struct {
	Kube     kube.Kube
	Pod      string
	services []string
}

func StartHelper(ctx context.Context, w io.Writer, c *cluster.Cluster, services []string) (*Helper, error) {
	suffix, err := config.GenerateSecret(6)
	if err != nil {
		return nil, err
	}
	name := HelperName + "-" + strings.ToLower(suffix)
	if err := c.Kube.Delete(ctx, w, "pod", "--selector", kube.NameLabel+"="+HelperName, "--wait=false"); err != nil {
		return nil, err
	}
	var env []map[string]any
	for _, service := range services {
		conn, err := c.Config.Database(service).LibpqURL()
		if err != nil {
			return nil, err
		}
		prefix := strings.ToUpper(service)
		env = append(env,
			map[string]any{"name": prefix + "_CONN", "value": conn},
			secretEnv(prefix+"_USER", service+"-db", "username"),
			secretEnv(prefix+"_PASS", service+"-db", "password"),
		)
	}
	pod := map[string]any{
		"apiVersion": "v1",
		"kind":       "Pod",
		"metadata": map[string]any{
			"name":      name,
			"namespace": c.Kube.Namespace,
			"labels":    map[string]string{kube.PartOfLabel: kube.PartOfValue, kube.NameLabel: HelperName},
		},
		"spec": map[string]any{
			"restartPolicy":                 "Never",
			"terminationGracePeriodSeconds": 0,
			"activeDeadlineSeconds":         HelperLifetimeSeconds,
			"containers": []map[string]any{{
				"name":    "psql",
				"image":   images.PostgresImage,
				"command": []string{"sleep", strconv.Itoa(HelperLifetimeSeconds)},
				"env":     env,
				"resources": map[string]any{
					"requests": map[string]string{"memory": "32Mi"},
					"limits":   map[string]string{"memory": "128Mi"},
				},
			}},
		},
	}
	if err := c.Kube.ApplyObject(ctx, w, pod); err != nil {
		return nil, err
	}
	helper := &Helper{Kube: c.Kube, Pod: name, services: services}
	if err := c.Kube.Cmd("wait", "--for=condition=Ready", "pod/"+name, "--timeout="+HelperReady.String()).Run(ctx, w); err != nil {
		helper.Close()
		return nil, err
	}
	return helper, nil
}

func secretEnv(name string, secret string, key string) map[string]any {
	return map[string]any{
		"name":      name,
		"valueFrom": map[string]any{"secretKeyRef": map[string]string{"name": secret, "key": key}},
	}
}

func (h *Helper) Query(ctx context.Context, database string, script string, vars map[string]string) (string, error) {
	prefix := strings.ToUpper(database)
	shell := fmt.Sprintf(`PGPASSWORD="$%s_PASS" exec psql "$@" --username="$%s_USER" --dbname="$%s_CONN"`, prefix, prefix, prefix)
	args := append([]string{"exec", "-i", h.Pod, "--", "sh", "-c", shell, "psql"}, psqlArgs(vars)...)
	return h.Kube.Cmd(args...).StdinString(script).Output(ctx)
}

func (h *Helper) Close() {
	ctx, cancel := context.WithTimeout(context.Background(), HelperCleanup)
	defer cancel()
	_ = h.Kube.Cmd("delete", "pod", h.Pod, "--ignore-not-found", "--wait=false").Run(ctx, io.Discard)
}

type Renewing struct {
	open    func(ctx context.Context) (Postgres, error)
	current Postgres
	opened  time.Time
}

func NewRenewing(open func(ctx context.Context) (Postgres, error)) *Renewing {
	return &Renewing{open: open}
}

func (r *Renewing) Query(ctx context.Context, database string, script string, vars map[string]string) (string, error) {
	if r.current == nil || time.Since(r.opened) > HelperRenewAfter {
		if r.current != nil {
			r.current.Close()
			r.current = nil
		}
		postgres, err := r.open(ctx)
		if err != nil {
			return "", err
		}
		r.current = postgres
		r.opened = time.Now()
	}
	return r.current.Query(ctx, database, script, vars)
}

func (r *Renewing) Close() {
	if r.current != nil {
		r.current.Close()
	}
}
