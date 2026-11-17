package config

import (
	"fmt"
	"net/url"
	"strings"

	"github.com/pelletier/go-toml/v2"
)

const (
	TargetCompose = "compose"
	TargetK8s     = "k8s"

	ClusterKind     = "kind"
	ClusterExisting = "existing"

	ModeLocal    = "local"
	ModeExternal = "external"

	MailMailpit  = "mailpit"
	MailExternal = "external"

	SecurityStartTLS = "starttls"
	SecurityTLS      = "tls"
	SecurityNone     = "none"

	PostgresPort           = 5432
	DefaultNamespace       = "codeclash"
	DefaultKindName        = "codeclash"
	DefaultPublicPort      = 8080
	DefaultWebHosts        = "localhost,127.0.0.1"
	DefaultOTLPEndpoint    = "http://grafana:4318"
	DefaultSampling        = "1.0"
	DefaultServiceMemory   = "768Mi"
	DefaultWebMemory       = "384Mi"
	DefaultDindMemory      = "3Gi"
	DefaultWarmSize        = 2
	DefaultSandboxMemory   = "512MB"
	DefaultMaxConcurrent   = 2
	DefaultPostgresStorage = "1Gi"
	DefaultDBUser          = "codeclash"
	DefaultSMTPPort        = 587
	DefaultSMTPTLSPort     = 465
	DefaultMailFrom        = "no-reply@codeclash.dev"
	DefaultAdminUsername   = "admin"
	DefaultAdminName       = "CodeClash Admin"
	LocalDomain            = "localhost"
	DefaultHTTPPort        = 80
	DefaultHTTPSPort       = 443
	KindProxyHops          = 1
	ExistingProxyHops      = 0
	IssuerStaging          = "staging"
	IssuerProduction       = "production"

	ToolGrafana = "grafana"
	ToolPgAdmin = "pgadmin"
	ToolKafkaUI = "kafka"
	ToolMailpit = "mail"
)

var ToolNames = []string{ToolGrafana, ToolPgAdmin, ToolKafkaUI, ToolMailpit}

var DBServices = []string{"auth", "problem", "contest", "runner", "notification"}

type Profile struct {
	Name        string      `toml:"-"`
	Target      string      `toml:"target"`
	Admin       Admin       `toml:"admin"`
	Contestants Contestants `toml:"contestants"`
	K8s         *K8s        `toml:"k8s,omitempty"`
}

type Contestants struct {
	Password string `toml:"password"`
}

type Admin struct {
	Username string `toml:"username"`
	Name     string `toml:"name"`
	Email    string `toml:"email"`
	Password string `toml:"password"`
}

type K8s struct {
	Cluster             string               `toml:"cluster"`
	KindName            string               `toml:"kind_name"`
	Context             string               `toml:"context"`
	Namespace           string               `toml:"namespace"`
	PublicPort          int                  `toml:"public_port"`
	Registry            string               `toml:"registry"`
	WebAllowedHosts     string               `toml:"web_allowed_hosts"`
	Grafana             bool                 `toml:"grafana"`
	OTLPEndpoint        string               `toml:"otlp_endpoint"`
	TracingSampling     string               `toml:"tracing_sampling"`
	ServiceMemoryLimit  string               `toml:"service_memory_limit"`
	WebMemoryLimit      string               `toml:"web_memory_limit"`
	DindMemoryLimit     string               `toml:"dind_memory_limit"`
	RunnerWarmSize      int                  `toml:"runner_warm_size"`
	SandboxMemory       string               `toml:"sandbox_memory"`
	RunnerMaxConcurrent int                  `toml:"runner_max_concurrent"`
	PostgresStorage     string               `toml:"postgres_storage"`
	Domain              string               `toml:"domain"`
	HTTPPort            int                  `toml:"http_port"`
	HTTPSPort           int                  `toml:"https_port"`
	TrustedProxyHops    int                  `toml:"trusted_proxy_hops"`
	TLS                 TLS                  `toml:"tls"`
	Tools               Tools                `toml:"tools"`
	Ops                 Ops                  `toml:"ops"`
	Mail                Mail                 `toml:"mail"`
	Databases           map[string]*Database `toml:"databases"`
}

type TLS struct {
	Enabled bool   `toml:"enabled"`
	Issuer  string `toml:"issuer"`
	Email   string `toml:"email"`
}

type Tools struct {
	Grafana bool `toml:"grafana"`
	PgAdmin bool `toml:"pgadmin"`
	KafkaUI bool `toml:"kafka_ui"`
	Mailpit bool `toml:"mailpit"`
}

type Ops struct {
	Email    string `toml:"email"`
	Password string `toml:"password"`
}

type Mail struct {
	Mode     string `toml:"mode"`
	Host     string `toml:"host"`
	Port     int    `toml:"port"`
	Security string `toml:"security"`
	User     string `toml:"user"`
	Password string `toml:"password"`
	From     string `toml:"from"`
}

type Database struct {
	Mode     string `toml:"mode"`
	URL      string `toml:"url"`
	User     string `toml:"user"`
	Password string `toml:"password"`
}

func NewProfile(name string, target string) (*Profile, error) {
	password, err := GeneratePassword()
	if err != nil {
		return nil, err
	}
	contestantPassword, err := GeneratePassword()
	if err != nil {
		return nil, err
	}
	profile := &Profile{
		Name:        name,
		Target:      target,
		Admin:       Admin{Username: DefaultAdminUsername, Name: DefaultAdminName, Password: password},
		Contestants: Contestants{Password: contestantPassword},
	}
	if target == TargetK8s {
		k8s, err := NewK8s()
		if err != nil {
			return nil, err
		}
		profile.K8s = k8s
	}
	return profile, nil
}

func NewK8s() (*K8s, error) {
	opsPassword, err := GeneratePassword()
	if err != nil {
		return nil, err
	}
	k8s := &K8s{
		Domain:              LocalDomain,
		HTTPPort:            DefaultHTTPPort,
		HTTPSPort:           DefaultHTTPSPort,
		TrustedProxyHops:    KindProxyHops,
		TLS:                 TLS{Issuer: IssuerStaging},
		Tools:               Tools{Grafana: true, PgAdmin: true, KafkaUI: true, Mailpit: true},
		Ops:                 Ops{Password: opsPassword},
		Cluster:             ClusterKind,
		KindName:            DefaultKindName,
		Namespace:           DefaultNamespace,
		PublicPort:          DefaultPublicPort,
		WebAllowedHosts:     DefaultWebHosts,
		Grafana:             true,
		OTLPEndpoint:        DefaultOTLPEndpoint,
		TracingSampling:     DefaultSampling,
		ServiceMemoryLimit:  DefaultServiceMemory,
		WebMemoryLimit:      DefaultWebMemory,
		DindMemoryLimit:     DefaultDindMemory,
		RunnerWarmSize:      DefaultWarmSize,
		SandboxMemory:       DefaultSandboxMemory,
		RunnerMaxConcurrent: DefaultMaxConcurrent,
		PostgresStorage:     DefaultPostgresStorage,
		Mail:                Mail{Mode: MailMailpit, Port: DefaultSMTPPort, Security: SecurityStartTLS, From: DefaultMailFrom},
		Databases:           map[string]*Database{},
	}
	for _, service := range DBServices {
		database, err := NewLocalDatabase(service)
		if err != nil {
			return nil, err
		}
		k8s.Databases[service] = database
	}
	return k8s, nil
}

func NewLocalDatabase(service string) (*Database, error) {
	password, err := GenerateSecret(SecretLength)
	if err != nil {
		return nil, err
	}
	return &Database{Mode: ModeLocal, URL: LocalDatabaseURL(service), User: DefaultDBUser, Password: password}, nil
}

func LocalDatabaseURL(service string) string {
	return fmt.Sprintf("jdbc:postgresql://%s-db:%d/%s", service, PostgresPort, service)
}

func (p *Profile) Clone() (*Profile, error) {
	data, err := toml.Marshal(p)
	if err != nil {
		return nil, err
	}
	clone := &Profile{}
	if err := toml.Unmarshal(data, clone); err != nil {
		return nil, err
	}
	clone.Name = p.Name
	return clone, nil
}

func (p *Profile) IsK8s() bool {
	return p.Target == TargetK8s
}

func (k *K8s) KubeContext() string {
	if k.Cluster == ClusterKind {
		return "kind-" + k.KindName
	}
	return k.Context
}

func (k *K8s) IsKind() bool {
	return k.Cluster == ClusterKind
}

func (k *K8s) TLSActive() bool {
	return k.TLS.Enabled && !k.IsKind() && strings.Contains(k.Domain, ".")
}

func (k *K8s) ToolEnabled(tool string) bool {
	switch tool {
	case ToolGrafana:
		return k.Tools.Grafana && k.Grafana
	case ToolPgAdmin:
		return k.Tools.PgAdmin
	case ToolKafkaUI:
		return k.Tools.KafkaUI
	case ToolMailpit:
		return k.Tools.Mailpit && k.Mail.Mode == MailMailpit
	}
	return false
}

func (k *K8s) EnabledTools() []string {
	var tools []string
	for _, tool := range ToolNames {
		if k.ToolEnabled(tool) {
			tools = append(tools, tool)
		}
	}
	return tools
}

func (k *K8s) Host(tool string) string {
	if tool == "" {
		return k.Domain
	}
	return tool + "." + k.Domain
}

func (k *K8s) URL(tool string) string {
	host := k.Host(tool)
	switch {
	case k.IsKind():
		return fmt.Sprintf("http://%s:%d", host, k.PublicPort)
	case k.TLSActive() && k.HTTPSPort == DefaultHTTPSPort:
		return "https://" + host
	case k.TLSActive():
		return fmt.Sprintf("https://%s:%d", host, k.HTTPSPort)
	case k.HTTPPort == DefaultHTTPPort:
		return "http://" + host
	default:
		return fmt.Sprintf("http://%s:%d", host, k.HTTPPort)
	}
}

func (k *K8s) AllowedHosts() string {
	hosts := strings.Split(k.WebAllowedHosts, ",")
	for _, host := range hosts {
		if host == k.Domain {
			return k.WebAllowedHosts
		}
	}
	return k.WebAllowedHosts + "," + k.Domain
}

func (k *K8s) Database(service string) *Database {
	return k.Databases[service]
}

func (d *Database) IsLocal() bool {
	return d.Mode == ModeLocal
}

func (d *Database) Name() string {
	parsed, err := url.Parse(strings.TrimPrefix(d.URL, "jdbc:"))
	if err != nil {
		return d.URL
	}
	return strings.TrimPrefix(parsed.Path, "/")
}

func (d *Database) LibpqURL() (string, error) {
	parsed, err := url.Parse(strings.TrimPrefix(d.URL, "jdbc:"))
	if err != nil {
		return "", fmt.Errorf("invalid JDBC URL %s: %w", d.URL, err)
	}
	query := url.Values{}
	original := parsed.Query()
	if mode := original.Get("sslmode"); mode != "" {
		query.Set("sslmode", mode)
	} else if original.Get("ssl") == "true" {
		query.Set("sslmode", "require")
	}
	parsed.RawQuery = query.Encode()
	return parsed.String(), nil
}

func (m *Mail) Authenticated() bool {
	return m.Mode == MailExternal && m.User != ""
}
