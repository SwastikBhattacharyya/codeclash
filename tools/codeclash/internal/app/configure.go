package app

import (
	"context"
	"errors"
	"fmt"
	"slices"
	"strconv"
	"strings"

	"github.com/charmbracelet/huh"

	"codeclash/internal/config"
	"codeclash/internal/kube"
	"codeclash/internal/ui"
)

const (
	layoutAllLocal   = "all-local"
	layoutPerService = "per-service"
	newProfileOption = "\x00new"
)

func run(fields ...huh.Field) error {
	return ui.Normalize(huh.NewForm(huh.NewGroup(fields...)).WithTheme(huh.ThemeCharm()).Run())
}

func intField(value *int, text *string) func() error {
	return func() error {
		parsed, err := strconv.Atoi(strings.TrimSpace(*text))
		if err != nil {
			return err
		}
		*value = parsed
		return nil
	}
}

func (a *App) interactiveOnly(action string) error {
	if !a.Console.Interactive() {
		return a.Console.Errorf("%s needs an interactive terminal", action)
	}
	return nil
}

func (a *App) NewProfile(ctx context.Context) error {
	if err := a.interactiveOnly("creating a profile"); err != nil {
		return err
	}
	var name string
	target := config.TargetK8s
	err := run(
		huh.NewInput().Title("Profile name").Description("e.g. kind, compose or vps. Stored in "+a.Store.Dir()).Value(&name).
			Validate(func(value string) error {
				if err := config.ValidateProfileName(value); err != nil {
					return err
				}
				if a.Store.Exists(value) {
					return errors.New("a profile with this name exists")
				}
				return nil
			}),
		huh.NewSelect[string]().Title("What does this profile manage?").Value(&target).Options(
			huh.NewOption("Kubernetes: kind, k3s or any cluster in your kubeconfig", config.TargetK8s),
			huh.NewOption("docker compose infrastructure for running the services in the IDE", config.TargetCompose),
		),
	)
	if err != nil {
		return err
	}
	profile, err := config.NewProfile(name, target)
	if err != nil {
		return a.Console.Errorf("%v", err)
	}
	if err := a.edit(ctx, profile); err != nil {
		return err
	}
	if err := a.Store.SetActive(name); err != nil {
		return a.Console.Errorf("%v", err)
	}
	a.Profile = profile
	return nil
}

func (a *App) Configure(ctx context.Context) error {
	if err := a.interactiveOnly("configure"); err != nil {
		return err
	}
	if a.Profile == nil {
		return a.NewProfile(ctx)
	}
	draft, err := a.Profile.Clone()
	if err != nil {
		return a.Console.Errorf("%v", err)
	}
	if err := a.edit(ctx, draft); err != nil {
		return err
	}
	a.Profile = draft
	return nil
}

func (a *App) edit(ctx context.Context, profile *config.Profile) error {
	a.Console.Section("Superadmin")
	if err := a.editAdmin(&profile.Admin); err != nil {
		return err
	}
	a.Console.Section("Contestants")
	if err := a.editContestants(&profile.Contestants); err != nil {
		return err
	}
	if profile.IsK8s() {
		if profile.K8s == nil {
			k8s, err := config.NewK8s()
			if err != nil {
				return a.Console.Errorf("%v", err)
			}
			profile.K8s = k8s
		}
		if err := a.editK8s(ctx, profile.K8s, profile.Admin); err != nil {
			return err
		}
	}
	if err := profile.Validate(); err != nil {
		return a.Console.Errorf("%v", err)
	}
	if err := a.Store.Save(profile); err != nil {
		return a.Console.Errorf("saving profile: %v", err)
	}
	a.Console.OK("Saved profile %s to %s (mode 0600)", profile.Name, a.Store.Dir())
	return nil
}

func (a *App) editAdmin(admin *config.Admin) error {
	return run(
		huh.NewNote().Title("Superadmin").Description("Seeded straight into the auth database. Reseeding resets the account to these values."),
		huh.NewInput().Title("Username").Value(&admin.Username).Validate(config.ValidateAdminUsername),
		huh.NewInput().Title("Display name").Value(&admin.Name).Validate(config.ValidateAdminName),
		huh.NewInput().Title("Email").Value(&admin.Email).Validate(config.ValidateAdminEmail),
		huh.NewInput().Title("Password").Description("A generated one is filled in. It is stored in the profile file.").
			EchoMode(huh.EchoModePassword).Value(&admin.Password).Validate(config.ValidateAdminPassword),
	)
}

func (a *App) editContestants(contestants *config.Contestants) error {
	if contestants.Password == "" {
		password, err := config.GeneratePassword()
		if err != nil {
			return a.Console.Errorf("%v", err)
		}
		contestants.Password = password
	}
	return run(
		huh.NewNote().Title("Contestants").Description("seed-contests signs up the dummy contestants in seed/contests/users.json with this shared password."),
		huh.NewInput().Title("Password").Description("A generated one is filled in. It is stored in the profile file.").
			EchoMode(huh.EchoModePassword).Value(&contestants.Password).Validate(config.ValidateAdminPassword),
	)
}

func (a *App) editK8s(ctx context.Context, k *config.K8s, admin config.Admin) error {
	a.Console.Section("Cluster")
	previous := k.Cluster
	if err := run(huh.NewSelect[string]().Title("Which cluster?").Value(&k.Cluster).Options(
		huh.NewOption("kind cluster created and torn down by this tool", config.ClusterKind),
		huh.NewOption("existing cluster from your kubeconfig (k3s, kind or any other)", config.ClusterExisting),
	)); err != nil {
		return err
	}
	if k.Cluster != previous {
		k.TrustedProxyHops = config.ExistingProxyHops
		if k.IsKind() {
			k.TrustedProxyHops = config.KindProxyHops
		}
		k.TLS.Enabled = !k.IsKind() && k.Domain != config.LocalDomain
	}
	if k.Cluster == config.ClusterKind {
		port := strconv.Itoa(k.PublicPort)
		if err := run(
			huh.NewInput().Title("kind cluster name").Value(&k.KindName).Validate(config.ValidateName),
			huh.NewInput().Title("Host port the frontend is published on").Description("1024-65535, changing it recreates the cluster").
				Value(&port).Validate(config.ValidateHostPort),
		); err != nil {
			return err
		}
		if err := intField(&k.PublicPort, &port)(); err != nil {
			return err
		}
	} else {
		contexts, err := kube.Contexts(ctx)
		if err != nil {
			return a.Console.Errorf("reading kubeconfig contexts: %v", err)
		}
		if len(contexts) == 0 {
			return a.Console.Errorf("no kubeconfig contexts found")
		}
		if k.Context == "" {
			k.Context = contexts[0]
		}
		if err := run(huh.NewSelect[string]().Title("kubeconfig context").Value(&k.Context).Options(huh.NewOptions(contexts...)...)); err != nil {
			return err
		}
	}
	if err := run(
		huh.NewInput().Title("Namespace").Value(&k.Namespace).Validate(config.ValidateName),
		huh.NewInput().Title("Image registry (optional)").
			Description("Leave blank for kind or a k3s on this machine. Other clusters pull from here, e.g. registry.example.com/team").
			Value(&k.Registry).Validate(config.ValidateRegistry),
		huh.NewInput().Title("Hostnames the frontend is reached at (comma separated)").
			Description("*.example.com covers subdomains but not example.com. Add your tunnel's domain, e.g. *.ngrok-free.app").
			Value(&k.WebAllowedHosts).Validate(config.ValidateHostList),
	); err != nil {
		return err
	}
	a.Console.Section("Databases")
	if err := a.editDatabases(k); err != nil {
		return err
	}
	a.Console.Section("Mail")
	if err := a.editMail(&k.Mail); err != nil {
		return err
	}
	a.Console.Section("Public access")
	if err := a.editPublic(k, admin); err != nil {
		return err
	}
	a.Console.Section("Advanced")
	return a.editAdvanced(k)
}

func toolOptions(k *config.K8s) []huh.Option[string] {
	var options []huh.Option[string]
	if k.Grafana {
		options = append(options, huh.NewOption("Grafana (grafana.<domain>)", config.ToolGrafana))
	}
	options = append(options,
		huh.NewOption("pgAdmin (pgadmin.<domain>)", config.ToolPgAdmin),
		huh.NewOption("Kafka UI (kafka.<domain>)", config.ToolKafkaUI))
	if k.Mail.Mode == config.MailMailpit {
		options = append(options, huh.NewOption("Mailpit (mail.<domain>)", config.ToolMailpit))
	}
	return options
}

func (a *App) editPublic(k *config.K8s, admin config.Admin) error {
	if k.Ops.Email == "" {
		k.Ops.Email = admin.Email
	}
	if k.TLS.Issuer == "" {
		k.TLS.Issuer = config.IssuerStaging
	}
	previousDomain := k.Domain
	tools := []string{}
	for _, item := range []struct {
		name    string
		enabled bool
	}{{config.ToolGrafana, k.Tools.Grafana}, {config.ToolPgAdmin, k.Tools.PgAdmin}, {config.ToolKafkaUI, k.Tools.KafkaUI}, {config.ToolMailpit, k.Tools.Mailpit}} {
		if item.enabled {
			tools = append(tools, item.name)
		}
	}
	hops := strconv.Itoa(k.TrustedProxyHops)
	fields := []huh.Field{
		huh.NewNote().Title("Public access").Description("The frontend is served at the domain and each tool at <tool>.<domain>. " +
			"With a real domain, point an A record for the domain and one for *.domain at the server."),
		huh.NewInput().Title("Domain").Description("localhost works locally: grafana.localhost resolves in browsers").Value(&k.Domain).Validate(config.ValidateDomain),
		huh.NewMultiSelect[string]().Title("Publish these tools").Options(toolOptions(k)...).Value(&tools),
		huh.NewInput().Title("Ops email").Description("The shared login for every tool. pgAdmin needs a real-looking email.").Value(&k.Ops.Email).Validate(config.ValidateOpsEmail),
		huh.NewInput().Title("Ops password").Description("Generated; stored in the profile").EchoMode(huh.EchoModePassword).Value(&k.Ops.Password).Validate(config.ValidateOpsPassword),
		huh.NewInput().Title("Trusted proxy hops").Description("1 behind a tunnel or Cloudflare's proxy, 0 when clients connect directly").Value(&hops).Validate(config.ValidateProxyHops),
	}
	if err := run(fields...); err != nil {
		return err
	}
	k.Tools = config.Tools{
		Grafana: slices.Contains(tools, config.ToolGrafana),
		PgAdmin: slices.Contains(tools, config.ToolPgAdmin),
		KafkaUI: slices.Contains(tools, config.ToolKafkaUI),
		Mailpit: slices.Contains(tools, config.ToolMailpit),
	}
	if err := intField(&k.TrustedProxyHops, &hops)(); err != nil {
		return err
	}
	if k.IsKind() {
		k.TLS.Enabled = false
		return nil
	}
	httpPort, httpsPort := strconv.Itoa(k.HTTPPort), strconv.Itoa(k.HTTPSPort)
	if err := run(
		huh.NewInput().Title("HTTP port of the load balancer").Value(&httpPort).Validate(config.ValidatePort),
		huh.NewInput().Title("HTTPS port of the load balancer").Value(&httpsPort).Validate(func(value string) error {
			if err := config.ValidatePort(value); err != nil {
				return err
			}
			if value == httpPort {
				return errors.New("must differ from the HTTP port")
			}
			return nil
		}),
	); err != nil {
		return err
	}
	if err := intField(&k.HTTPPort, &httpPort)(); err != nil {
		return err
	}
	if err := intField(&k.HTTPSPort, &httpsPort)(); err != nil {
		return err
	}
	if !strings.Contains(k.Domain, ".") {
		k.TLS.Enabled = false
		return nil
	}
	if k.TLS.Email == "" {
		k.TLS.Email = k.Ops.Email
	}
	if previousDomain == config.LocalDomain {
		k.TLS.Enabled = true
	}
	if err := run(huh.NewConfirm().Title("Serve HTTPS with Let's Encrypt certificates?").
		Description("cert-manager solves HTTP-01 through the gateway, so DNS must already point here").Value(&k.TLS.Enabled)); err != nil {
		return err
	}
	if !k.TLS.Enabled {
		return nil
	}
	return run(
		huh.NewSelect[string]().Title("Let's Encrypt environment").Value(&k.TLS.Issuer).Options(
			huh.NewOption("Staging (untrusted certificates, generous rate limits: try this first)", config.IssuerStaging),
			huh.NewOption("Production", config.IssuerProduction),
		),
		huh.NewInput().Title("ACME account email").Value(&k.TLS.Email).Validate(config.ValidateEmail),
	)
}

func (a *App) editDatabases(k *config.K8s) error {
	layout := layoutAllLocal
	for _, service := range config.DBServices {
		if database := k.Database(service); database != nil && !database.IsLocal() {
			layout = layoutPerService
		}
	}
	if err := run(huh.NewSelect[string]().Title("Where should the databases live?").Value(&layout).Options(
		huh.NewOption("In-cluster Postgres for every service", layoutAllLocal),
		huh.NewOption("Choose per service", layoutPerService),
	)); err != nil {
		return err
	}
	for _, service := range config.DBServices {
		database := k.Database(service)
		if database == nil {
			fresh, err := config.NewLocalDatabase(service)
			if err != nil {
				return a.Console.Errorf("%v", err)
			}
			database = fresh
			k.Databases[service] = database
		}
		mode := config.ModeLocal
		if layout == layoutPerService {
			mode = database.Mode
			if err := run(huh.NewSelect[string]().Title(service+" database").Value(&mode).Options(
				huh.NewOption("In-cluster Postgres", config.ModeLocal),
				huh.NewOption("External database", config.ModeExternal),
			)); err != nil {
				return err
			}
		}
		if err := a.editDatabase(service, database, mode); err != nil {
			return err
		}
	}
	return nil
}

func (a *App) editDatabase(service string, database *config.Database, mode string) error {
	if mode == config.ModeLocal {
		if !database.IsLocal() {
			fresh, err := config.NewLocalDatabase(service)
			if err != nil {
				return a.Console.Errorf("%v", err)
			}
			*database = *fresh
		}
		database.URL = config.LocalDatabaseURL(service)
		return nil
	}
	if database.IsLocal() {
		*database = config.Database{Mode: config.ModeExternal, URL: fmt.Sprintf("jdbc:postgresql://db.example.com:%d/%s", config.PostgresPort, service), User: config.DefaultDBUser}
	}
	return run(
		huh.NewInput().Title(service+" JDBC URL").Description("The cluster must be able to reach it. Keep the password out of the URL.").
			Value(&database.URL).Validate(config.ValidateJDBC),
		huh.NewInput().Title(service+" username").Value(&database.User).Validate(config.ValidateLooseName),
		huh.NewInput().Title(service+" password").EchoMode(huh.EchoModePassword).Value(&database.Password).Validate(config.ValidateSecret),
	)
}

func (a *App) editMail(mail *config.Mail) error {
	if err := run(huh.NewSelect[string]().Title("How should notification-service send mail?").Value(&mail.Mode).Options(
		huh.NewOption("In-cluster Mailpit (catches every mail, nothing is delivered)", config.MailMailpit),
		huh.NewOption("External SMTP server", config.MailExternal),
	)); err != nil {
		return err
	}
	if mail.Mode == config.MailMailpit {
		return run(huh.NewInput().Title("Sender address").Value(&mail.From).Validate(config.ValidateEmail))
	}
	if err := run(
		huh.NewInput().Title("SMTP host").Value(&mail.Host).Validate(config.ValidateHost),
		huh.NewSelect[string]().Title("Connection security").Value(&mail.Security).Options(
			huh.NewOption("STARTTLS", config.SecurityStartTLS),
			huh.NewOption("SSL/TLS", config.SecurityTLS),
			huh.NewOption("None", config.SecurityNone),
		),
	); err != nil {
		return err
	}
	switch {
	case mail.Security == config.SecurityTLS && mail.Port == config.DefaultSMTPPort:
		mail.Port = config.DefaultSMTPTLSPort
	case mail.Security != config.SecurityTLS && mail.Port == config.DefaultSMTPTLSPort:
		mail.Port = config.DefaultSMTPPort
	}
	port := strconv.Itoa(mail.Port)
	if err := run(
		huh.NewInput().Title("SMTP port").Value(&port).Validate(config.ValidatePort),
		huh.NewInput().Title("SMTP username (blank for no authentication)").Value(&mail.User).Validate(func(value string) error {
			if value == "" {
				return nil
			}
			return config.ValidateLooseName(value)
		}),
		huh.NewInput().Title("Sender address").Value(&mail.From).Validate(config.ValidateEmail),
	); err != nil {
		return err
	}
	if err := intField(&mail.Port, &port)(); err != nil {
		return err
	}
	if mail.User == "" {
		mail.Password = ""
		return nil
	}
	return run(huh.NewInput().Title("SMTP password").EchoMode(huh.EchoModePassword).Value(&mail.Password).Validate(config.ValidateSecret))
}

func (a *App) editAdvanced(k *config.K8s) error {
	customize := false
	if err := run(huh.NewConfirm().Title("Customize advanced settings (observability, resources, storage)?").Value(&customize)); err != nil {
		return err
	}
	if !customize {
		return nil
	}
	warm, sandboxes := strconv.Itoa(k.RunnerWarmSize), strconv.Itoa(k.RunnerMaxConcurrent)
	if err := run(
		huh.NewConfirm().Title("Deploy in-cluster Grafana (logs, traces, metrics)?").Value(&k.Grafana),
		huh.NewInput().Title("OTLP HTTP endpoint").Description("Point it at your own collector when Grafana is off").Value(&k.OTLPEndpoint).Validate(config.ValidateURL),
		huh.NewInput().Title("Tracing sampling probability").Value(&k.TracingSampling).Validate(config.ValidateProbability),
		huh.NewInput().Title("Memory limit per Spring service").Value(&k.ServiceMemoryLimit).Validate(config.ValidateK8sSize),
		huh.NewInput().Title("Memory limit for the frontend").Value(&k.WebMemoryLimit).Validate(config.ValidateK8sSize),
		huh.NewInput().Title("Memory limit for the runner's Docker sidecar").
			Description("(warm + concurrent) sandboxes at the largest question memory limit, plus ~300 MB").
			Value(&k.DindMemoryLimit).Validate(config.ValidateK8sSize),
		huh.NewInput().Title("Warm sandbox containers").Value(&warm).Validate(config.ValidateCount),
		huh.NewInput().Title("Memory per sandbox container").Value(&k.SandboxMemory).Validate(config.ValidateSpringSize),
		huh.NewInput().Title("Concurrent submissions").Value(&sandboxes).Validate(config.ValidatePositiveCount),
		huh.NewInput().Title("Storage per in-cluster Postgres").Description("Fixed once a database exists").
			Value(&k.PostgresStorage).Validate(config.ValidateK8sSize),
	); err != nil {
		return err
	}
	if err := intField(&k.RunnerWarmSize, &warm)(); err != nil {
		return err
	}
	return intField(&k.RunnerMaxConcurrent, &sandboxes)()
}

func (a *App) SwitchProfile(ctx context.Context) error {
	if err := a.interactiveOnly("switching profiles"); err != nil {
		return err
	}
	names, err := a.Store.List()
	if err != nil {
		return a.Console.Errorf("%v", err)
	}
	var options []huh.Option[string]
	for _, name := range names {
		options = append(options, huh.NewOption(name, name))
	}
	options = append(options, huh.NewOption("New profile…", newProfileOption))
	choice := ""
	if a.Profile != nil {
		choice = a.Profile.Name
	}
	if err := run(huh.NewSelect[string]().Title("Profile").Value(&choice).Options(options...)); err != nil {
		return err
	}
	if choice == newProfileOption {
		return a.NewProfile(ctx)
	}
	return a.Use(choice)
}

func (a *App) Use(name string) error {
	profile, err := a.Store.Load(name)
	if err != nil {
		return a.Console.Errorf("%v", err)
	}
	if err := a.Store.SetActive(name); err != nil {
		return a.Console.Errorf("%v", err)
	}
	a.Profile = profile
	a.Console.OK("Active profile: %s (%s)", name, profile.Target)
	return nil
}
