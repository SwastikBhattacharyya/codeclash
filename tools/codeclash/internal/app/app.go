package app

import (
	"context"
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"

	"codeclash/internal/cluster"
	"codeclash/internal/compose"
	"codeclash/internal/config"
	"codeclash/internal/deploy"
	"codeclash/internal/execx"
	"codeclash/internal/images"
	"codeclash/internal/kube"
	"codeclash/internal/pg"
	"codeclash/internal/repo"
	"codeclash/internal/reset"
	"codeclash/internal/seed"
	"codeclash/internal/ui"
)

const GatewayServicePort = 8080

type App struct {
	Repo    *repo.Repo
	Store   *config.Store
	Console *ui.Console
	Profile *config.Profile
}

func (a *App) require(tools ...string) error {
	var missing []string
	for _, tool := range tools {
		if !execx.Exists(tool) {
			missing = append(missing, tool)
		}
	}
	if len(missing) > 0 {
		return a.Console.Errorf("missing tools: %s", strings.Join(missing, ", "))
	}
	return nil
}

func (a *App) ready() error {
	if a.Profile == nil {
		return a.Console.Errorf("%v: run codeclash configure", config.ErrNoProfile)
	}
	if err := a.Profile.Validate(); err != nil {
		return a.Console.Errorf("%v", err)
	}
	return nil
}

func (a *App) needK8s(action string) error {
	if err := a.ready(); err != nil {
		return err
	}
	if !a.Profile.IsK8s() {
		return a.Console.Errorf("%s needs a k8s profile, %s targets compose", action, a.Profile.Name)
	}
	tools := []string{"docker", "kubectl"}
	if a.Profile.K8s.Cluster == config.ClusterKind {
		tools = append(tools, "kind")
	}
	return a.require(tools...)
}

func (a *App) needCompose(action string) error {
	if err := a.ready(); err != nil {
		return err
	}
	if a.Profile.IsK8s() {
		return a.Console.Errorf("%s needs a compose profile, %s targets k8s", action, a.Profile.Name)
	}
	return a.require("docker")
}

func (a *App) cluster() *cluster.Cluster {
	return cluster.New(a.Repo, a.Profile)
}

func (a *App) compose() (*compose.Compose, error) {
	c, err := compose.Open(a.Repo)
	if err != nil {
		return nil, a.Console.Errorf("%v", err)
	}
	return c, nil
}

func (a *App) Build(ctx context.Context) error {
	if err := a.needK8s("build"); err != nil {
		return err
	}
	if out, err := execx.Command("docker", "buildx", "version").Output(ctx); err != nil {
		return a.Console.Errorf("docker-buildx is not installed: %v %s", err, out)
	}
	set, err := images.Resolve(ctx, a.Repo, a.Profile.K8s.Registry)
	if err != nil {
		return a.Console.Errorf("%v", err)
	}
	a.Console.Section("Build images")
	for _, service := range images.Services {
		if err := a.Console.Run(ctx, "Build "+set.Ref(service), func(ctx context.Context, w io.Writer) error {
			return set.BuildService(ctx, w, a.Repo, service)
		}); err != nil {
			return err
		}
	}
	if err := a.Console.Run(ctx, "Build sandbox image "+images.SandboxImage, func(ctx context.Context, w io.Writer) error {
		return images.BuildSandbox(ctx, w, a.Repo)
	}); err != nil {
		return err
	}
	return a.Console.Run(ctx, "Bundle sandbox image "+set.Ref(images.SandboxBundle), set.BuildBundle)
}

func (a *App) Deploy(ctx context.Context) error {
	if err := a.needK8s("deploy"); err != nil {
		return err
	}
	return deploy.New(a.cluster(), a.Console, a.Store).Run(ctx)
}

func (a *App) Reset(ctx context.Context) error {
	if a.Profile != nil && a.Profile.IsK8s() {
		if err := a.needK8s("reset"); err != nil {
			return err
		}
		c := a.cluster()
		if _, managed := c.ManagedKind(); managed && !c.ManagedKindExists(ctx) {
			a.Console.Info("kind cluster %s does not exist, nothing to reset", a.Profile.K8s.KindName)
			return nil
		}
		if err := c.Kube.Reachable(ctx); err != nil {
			return a.Console.Errorf("%v", err)
		}
		return reset.K8s(ctx, a.Console, c)
	}
	if err := a.needCompose("reset"); err != nil {
		return err
	}
	c, err := a.compose()
	if err != nil {
		return err
	}
	return reset.Compose(ctx, a.Console, c)
}

func (a *App) postgres(ctx context.Context) (pg.Postgres, string, error) {
	open, hint, err := a.postgresOpener(ctx)
	if err != nil {
		return nil, "", err
	}
	var postgres pg.Postgres
	if err := a.Console.Run(ctx, "Connect to Postgres", func(ctx context.Context, w io.Writer) error {
		var err error
		postgres, err = open(ctx)
		return err
	}); err != nil {
		return nil, "", err
	}
	return postgres, hint, nil
}

func (a *App) postgresOpener(ctx context.Context) (func(ctx context.Context) (pg.Postgres, error), string, error) {
	if a.Profile.IsK8s() {
		c := a.cluster()
		if err := c.Kube.Reachable(ctx); err != nil {
			return nil, "", a.Console.Errorf("%v", err)
		}
		if !c.Kube.NamespaceExists(ctx) {
			return nil, "", a.Console.Errorf("namespace %s does not exist, deploy first", c.Kube.Namespace)
		}
		open := func(ctx context.Context) (pg.Postgres, error) {
			return pg.StartHelper(ctx, io.Discard, c, []string{"auth", "problem", "contest"})
		}
		return open, "is auth-service running? deploy first", nil
	}
	c, err := a.compose()
	if err != nil {
		return nil, "", err
	}
	running, err := c.Running(ctx)
	if err != nil {
		return nil, "", a.Console.Errorf("docker compose: %v", err)
	}
	if !running[compose.DB] {
		return nil, "", a.Console.Errorf("the db container is not running, start the infrastructure first")
	}
	postgres, err := pg.NewCompose(c)
	if err != nil {
		return nil, "", a.Console.Errorf("%v", err)
	}
	open := func(ctx context.Context) (pg.Postgres, error) {
		return postgres, nil
	}
	return open, "start auth-service once so it syncs the default roles", nil
}

func (a *App) SeedAdmin(ctx context.Context) error {
	if err := a.ready(); err != nil {
		return err
	}
	if err := a.require("docker"); err != nil {
		return err
	}
	postgres, hint, err := a.postgres(ctx)
	if err != nil {
		return err
	}
	defer postgres.Close()
	return seed.Admin(ctx, a.Console, postgres, a.Profile.Admin, hint)
}

func (a *App) SeedQuestions(ctx context.Context, verify bool) error {
	if err := a.ready(); err != nil {
		return err
	}
	if err := a.require("docker"); err != nil {
		return err
	}
	options := seed.Options{Username: a.Profile.Admin.Username, Password: a.Profile.Admin.Password, Verify: verify}
	gateway, closeGateway, err := a.gateway(ctx)
	if err != nil {
		return err
	}
	defer closeGateway()
	return seed.Questions(ctx, a.Console, a.Repo, gateway, options)
}

func (a *App) SeedContests(ctx context.Context) error {
	if err := a.ready(); err != nil {
		return err
	}
	if err := a.require("docker"); err != nil {
		return err
	}
	if a.Profile.Contestants.Password == "" {
		return a.Console.Errorf("the profile has no contestant password, run configure")
	}
	options := seed.ContestOptions{
		Username:           a.Profile.Admin.Username,
		Password:           a.Profile.Admin.Password,
		ContestantPassword: a.Profile.Contestants.Password,
	}
	open, _, err := a.postgresOpener(ctx)
	if err != nil {
		return err
	}
	postgres := pg.NewRenewing(open)
	defer postgres.Close()
	gateway, closeGateway, err := a.gateway(ctx)
	if err != nil {
		return err
	}
	defer closeGateway()
	return seed.Contests(ctx, a.Console, a.Repo, gateway, postgres, options)
}

func (a *App) gateway(ctx context.Context) (string, func(), error) {
	if !a.Profile.IsK8s() {
		c, err := a.compose()
		if err != nil {
			return "", nil, err
		}
		gateway, err := c.URL("GATEWAY_PORT")
		if err != nil {
			return "", nil, a.Console.Errorf("%v", err)
		}
		return gateway, func() {}, nil
	}
	c := a.cluster()
	if err := c.Kube.Reachable(ctx); err != nil {
		return "", nil, a.Console.Errorf("%v", err)
	}
	var forward *kube.Forward
	if err := a.Console.Run(ctx, "Forward a port to gateway-service", func(ctx context.Context, w io.Writer) error {
		var err error
		forward, err = c.Kube.PortForward(ctx, "service/gateway-service", GatewayServicePort)
		return err
	}); err != nil {
		return "", nil, err
	}
	return forward.URL(), forward.Close, nil
}

func (a *App) Up(ctx context.Context) error {
	if err := a.needCompose("up"); err != nil {
		return err
	}
	c, err := a.compose()
	if err != nil {
		return err
	}
	a.Console.Section("Infrastructure")
	return a.Console.Run(ctx, "Start "+strings.Join(compose.Infra, ", "), c.Up)
}

func (a *App) Down(ctx context.Context) error {
	if err := a.needCompose("down"); err != nil {
		return err
	}
	c, err := a.compose()
	if err != nil {
		return err
	}
	a.Console.Section("Infrastructure")
	return a.Console.Run(ctx, "Stop the compose stack (volumes are kept)", c.Down)
}

func (a *App) Status(ctx context.Context) error {
	if err := a.ready(); err != nil {
		return err
	}
	a.Console.Section("Profile " + a.Profile.Name)
	a.Console.Table([]string{"Setting", "Value"}, a.Summary())
	if !a.Profile.IsK8s() {
		c, err := a.compose()
		if err != nil {
			return err
		}
		out, err := c.Status(ctx)
		if err != nil {
			return a.Console.Errorf("docker compose: %v", err)
		}
		a.Console.Section("Containers")
		fmt.Fprint(os.Stdout, indent(out))
		return nil
	}
	if set, err := images.Resolve(ctx, a.Repo, a.Profile.K8s.Registry); err == nil {
		a.Console.Section("Images for this checkout")
		var rows [][]string
		for _, ref := range set.Own() {
			state := "not built"
			if images.Present(ctx, ref) {
				state = "built"
			}
			rows = append(rows, []string{ref, state})
		}
		a.Console.Table([]string{"Image", "Local"}, rows)
	}
	c := a.cluster()
	a.Console.Section("Pods")
	out, err := c.Kube.Cmd("get", "pods", "-o", "wide").Output(ctx)
	if err != nil {
		a.Console.Warn("cannot list pods: %v", err)
		return nil
	}
	fmt.Fprint(os.Stdout, indent(out))
	return nil
}

func indent(text string) string {
	lines := strings.Split(strings.TrimRight(text, "\n"), "\n")
	return "  " + strings.Join(lines, "\n  ") + "\n"
}

func (a *App) Teardown(ctx context.Context) error {
	if err := a.needK8s("teardown"); err != nil {
		return err
	}
	c := a.cluster()
	a.Console.Section("Teardown")
	if kind, managed := c.ManagedKind(); managed {
		if !c.ManagedKindExists(ctx) {
			a.Console.Info("kind cluster %s does not exist", kind.Name)
			return nil
		}
		ok, err := a.Console.Confirm("Delete kind cluster "+kind.Name+" and all its data?", false)
		if err != nil || !ok {
			return err
		}
		return a.Console.Run(ctx, "Delete cluster "+kind.Name, kind.Delete)
	}
	if err := c.Kube.Reachable(ctx); err != nil {
		return a.Console.Errorf("%v", err)
	}
	ok, err := a.Console.ConfirmTyped("Delete namespace "+c.Kube.Namespace+" on "+c.Kube.Context+", including every database volume?", c.Kube.Namespace)
	if err != nil {
		return err
	}
	if !ok {
		a.Console.Info("Nothing deleted")
		return nil
	}
	return a.Console.Run(ctx, "Delete namespace "+c.Kube.Namespace, func(ctx context.Context, w io.Writer) error {
		if err := c.Kube.ClusterCmd("delete", "namespace", c.Kube.Namespace, "--ignore-not-found", "--wait=true").Run(ctx, w); err != nil {
			return err
		}
		return c.Kube.ClusterCmd("delete", "gatewayclass", "codeclash", "--ignore-not-found").Run(ctx, w)
	})
}

func (a *App) Summary() [][]string {
	p := a.Profile
	rows := [][]string{
		{"Profile", p.Name + " (" + p.Target + ")"},
		{"Superadmin", p.Admin.Username + " <" + p.Admin.Email + ">"},
	}
	if !p.IsK8s() {
		return append(rows, []string{"Stack", "docker compose with " + a.Repo.Path(".env")})
	}
	k := p.K8s
	if k.Cluster == config.ClusterKind {
		rows = append(rows, []string{"Cluster", "kind " + k.KindName + ", frontend on 127.0.0.1:" + strconv.Itoa(k.PublicPort)})
	} else {
		rows = append(rows, []string{"Cluster", "context " + k.Context + ", load balancer ports " + strconv.Itoa(k.HTTPPort) + "/" + strconv.Itoa(k.HTTPSPort)})
	}
	rows = append(rows, []string{"Namespace", k.Namespace}, []string{"Frontend", k.URL("")})
	for _, tool := range k.EnabledTools() {
		rows = append(rows, []string{"Tool " + tool, k.URL(tool) + " (ops: " + k.Ops.Email + ")"})
	}
	if k.TLSActive() {
		rows = append(rows, []string{"TLS", "Let's Encrypt " + k.TLS.Issuer + ", account " + k.TLS.Email})
	}
	if k.Registry != "" {
		rows = append(rows, []string{"Registry", k.Registry})
	}
	for _, service := range config.DBServices {
		database := k.Database(service)
		if database == nil {
			continue
		}
		where := "in-cluster"
		if !database.IsLocal() {
			where = database.URL + " as " + database.User
		}
		rows = append(rows, []string{service + " database", where})
	}
	mail := "in-cluster Mailpit"
	if k.Mail.Mode == config.MailExternal {
		mail = k.Mail.Host + ":" + strconv.Itoa(k.Mail.Port) + " (" + k.Mail.Security + ")"
		if k.Mail.User != "" {
			mail += " as " + k.Mail.User
		}
	}
	grafana := "off"
	if k.Grafana {
		grafana = "in-cluster"
	}
	return append(rows,
		[]string{"Mail", mail + ", from " + k.Mail.From},
		[]string{"Frontend hosts", k.WebAllowedHosts},
		[]string{"Grafana", grafana},
		[]string{"Telemetry", k.OTLPEndpoint + " (sampling " + k.TracingSampling + ")"},
		[]string{"Resources", "services " + k.ServiceMemoryLimit + ", frontend " + k.WebMemoryLimit + ", docker sidecar " + k.DindMemoryLimit + ", " + strconv.Itoa(k.RunnerWarmSize) + " warm sandboxes"},
	)
}
