package cluster

import (
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/config"
	"codeclash/internal/kube"
	"codeclash/internal/repo"
)

const (
	MigrateCommand      = "migrate"
	CleanMigrateCommand = "clean migrate"
	PullIfNotPresent    = "IfNotPresent"
	PullAlways          = "Always"
	MailpitHost         = "mailpit"
	MailpitSMTPPort     = 1025
)

var placeholder = regexp.MustCompile(`\$\{([A-Za-z_][A-Za-z0-9_]*)\}`)

type Cluster struct {
	Repo    *repo.Repo
	Profile *config.Profile
	Config  *config.K8s
	Kube    kube.Kube
}

func New(r *repo.Repo, profile *config.Profile) *Cluster {
	return &Cluster{
		Repo:    r,
		Profile: profile,
		Config:  profile.K8s,
		Kube:    kube.Kube{Context: profile.K8s.KubeContext(), Namespace: profile.K8s.Namespace},
	}
}

func (c *Cluster) ManagedKind() (kube.Kind, bool) {
	if c.Config.Cluster != config.ClusterKind {
		return kube.Kind{}, false
	}
	return kube.Kind{Name: c.Config.KindName, PublicPort: c.Config.PublicPort}, true
}

func (c *Cluster) ManagedKindExists(ctx context.Context) bool {
	kind, managed := c.ManagedKind()
	return managed && kind.Exists(ctx)
}

func (c *Cluster) Vars() map[string]string {
	k := c.Config
	mail := k.Mail
	host, port, ssl, starttls := mail.Host, strconv.Itoa(mail.Port), "false", "false"
	if mail.Mode == config.MailMailpit {
		host, port = MailpitHost, strconv.Itoa(MailpitSMTPPort)
	} else {
		switch mail.Security {
		case config.SecurityTLS:
			ssl = "true"
		case config.SecurityStartTLS:
			starttls = "true"
		}
	}
	vars := map[string]string{
		"NAMESPACE":             k.Namespace,
		"DEPLOY_ID":             time.Now().Format("20060102150405"),
		"OTLP_ENDPOINT":         k.OTLPEndpoint,
		"TRACING_SAMPLING":      k.TracingSampling,
		"SERVICE_MEMORY_LIMIT":  k.ServiceMemoryLimit,
		"WEB_MEMORY_LIMIT":      k.WebMemoryLimit,
		"DIND_MEMORY_LIMIT":     k.DindMemoryLimit,
		"RUNNER_WARM_SIZE":      strconv.Itoa(k.RunnerWarmSize),
		"SANDBOX_MEMORY":        k.SandboxMemory,
		"RUNNER_MAX_CONCURRENT": strconv.Itoa(k.RunnerMaxConcurrent),
		"POSTGRES_STORAGE":      k.PostgresStorage,
		"WEB_ALLOWED_HOSTS":     k.AllowedHosts(),
		"TRUSTED_PROXY_HOPS":    strconv.Itoa(k.TrustedProxyHops),
		"GATEWAY_NODE_PORT":     strconv.Itoa(kube.GatewayNodePort),
		"SMTP_HOST":             host,
		"SMTP_PORT":             port,
		"SMTP_SSL":              ssl,
		"SMTP_STARTTLS":         starttls,
		"SMTP_AUTH":             strconv.FormatBool(mail.Authenticated()),
		"MAIL_FROM":             mail.From,
		"IMAGE_PULL_POLICY":     PullIfNotPresent,
	}
	if k.Registry != "" {
		vars["IMAGE_PULL_POLICY"] = PullAlways
	}
	for _, service := range config.DBServices {
		vars[strings.ToUpper(service)+"_DB_URL"] = k.Database(service).URL
	}
	return vars
}

func Render(text string, vars map[string]string) (string, error) {
	var missing []string
	out := placeholder.ReplaceAllStringFunc(text, func(match string) string {
		name := placeholder.FindStringSubmatch(match)[1]
		value, ok := vars[name]
		if !ok {
			missing = append(missing, name)
			return match
		}
		return value
	})
	if len(missing) > 0 {
		return "", fmt.Errorf("no value for %s", strings.Join(missing, ", "))
	}
	return out, nil
}

func (c *Cluster) Manifest(path string, vars map[string]string, overrides map[string]string) (string, error) {
	data, err := os.ReadFile(c.Repo.Path(append([]string{"k8s"}, strings.Split(path, "/")...)...))
	if err != nil {
		return "", err
	}
	merged := make(map[string]string, len(vars)+len(overrides))
	for key, value := range vars {
		merged[key] = value
	}
	for key, value := range overrides {
		merged[key] = value
	}
	rendered, err := Render(string(data), merged)
	if err != nil {
		return "", fmt.Errorf("k8s/%s: %w", path, err)
	}
	return rendered, nil
}

func (c *Cluster) Apply(ctx context.Context, w io.Writer, path string, vars map[string]string, overrides map[string]string) error {
	manifest, err := c.Manifest(path, vars, overrides)
	if err != nil {
		return err
	}
	return c.Kube.Apply(ctx, w, manifest)
}

func (c *Cluster) Migrate(ctx context.Context, w io.Writer, vars map[string]string, service string, clean bool) error {
	dir := c.Repo.Path("app", service+"-service", "src", "main", "resources", "db", "migration")
	files, err := filepath.Glob(filepath.Join(dir, "*.sql"))
	if err != nil {
		return err
	}
	if len(files) == 0 {
		return fmt.Errorf("no migrations in %s", dir)
	}
	data := map[string]string{}
	for _, file := range files {
		content, err := os.ReadFile(file)
		if err != nil {
			return err
		}
		data[filepath.Base(file)] = string(content)
	}
	if err := c.Kube.ApplyConfigMap(ctx, w, service+"-migrations", data); err != nil {
		return err
	}
	job := service + "-migrate"
	if err := c.Kube.Delete(ctx, w, "job", job, "--wait=true"); err != nil {
		return err
	}
	command, cleanDisabled := MigrateCommand, "true"
	if clean {
		command, cleanDisabled = CleanMigrateCommand, "false"
	}
	overrides := map[string]string{
		"DB_SERVICE":            service,
		"DB_URL":                c.Config.Database(service).URL,
		"FLYWAY_COMMAND":        command,
		"FLYWAY_CLEAN_DISABLED": cleanDisabled,
	}
	if err := c.Apply(ctx, w, "migrations/flyway-job.yaml", vars, overrides); err != nil {
		return err
	}
	return c.Kube.WaitJob(ctx, w, job)
}

func (c *Cluster) LocalDatabases() []string {
	var services []string
	for _, service := range config.DBServices {
		if c.Config.Database(service).IsLocal() {
			services = append(services, service)
		}
	}
	return services
}
