package deploy

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"

	"codeclash/internal/cluster"
	"codeclash/internal/config"
	"codeclash/internal/execx"
	"codeclash/internal/images"
	"codeclash/internal/kube"
	"codeclash/internal/ui"
)

const (
	EnvoyGatewayVersion = "v1.10.0"
	EnvoyNamespace      = "envoy-gateway-system"
	InfraTimeout        = 5 * time.Minute
	GrafanaTimeout      = 10 * time.Minute
	ServiceTimeout      = 10 * time.Minute
	RouteTimeout        = 2 * time.Minute
	RoutePoll           = 2 * time.Second
	BundleTimeout       = 2 * time.Minute
	GatewayTimeout      = 5 * time.Minute
	NodeTimeout         = 3 * time.Minute
	DownloadAttempts    = 3
	DownloadBackoff     = 3 * time.Second
	MaxBundleBytes      = 64 << 20
	EnvoyCertgenJob     = "eg-gateway-helm-certgen"
	GatewayLocalPort    = 9000
	MailpitUIPort       = 8025
	GrafanaPort         = 3000
	ServicePort         = 8080
)

var GatewayCRDs = []string{
	"crd/envoyproxies.gateway.envoyproxy.io",
	"crd/clienttrafficpolicies.gateway.envoyproxy.io",
	"crd/gatewayclasses.gateway.networking.k8s.io",
	"crd/gateways.gateway.networking.k8s.io",
	"crd/httproutes.gateway.networking.k8s.io",
}

type rollout struct {
	resource string
	timeout  time.Duration
}

type Deployer struct {
	Cluster *cluster.Cluster
	Console *ui.Console
	Store   *config.Store
	loader  images.Loader
	set     *images.Set
	vars    map[string]string
}

func New(c *cluster.Cluster, console *ui.Console, store *config.Store) *Deployer {
	return &Deployer{Cluster: c, Console: console, Store: store}
}

func (d *Deployer) Run(ctx context.Context) error {
	console := d.Console
	k := d.Cluster.Config
	console.Section("Cluster")
	if err := Ensure(ctx, d.Cluster, console); err != nil {
		return err
	}
	loader, distro, err := Loader(ctx, d.Cluster, console)
	if err != nil {
		return err
	}
	d.loader = loader
	if err := d.checkTraefik(ctx, distro); err != nil {
		return err
	}
	set, err := images.Resolve(ctx, d.Cluster.Repo, k.Registry)
	if err != nil {
		return console.Errorf("%v", err)
	}
	d.set = set
	if missing := set.Missing(ctx); len(missing) > 0 {
		return console.Errorf("missing images, run build first: %s", strings.Join(missing, ", "))
	}
	if err := d.checkStorage(ctx); err != nil {
		return err
	}
	d.vars = d.Cluster.Vars()
	d.vars["IMAGE_PREFIX"] = set.Prefix()
	d.publicVars()
	steps := []func(context.Context) error{d.images, d.namespace, d.secrets, d.infra, d.migrate, d.services, d.gateway}
	for _, step := range steps {
		if err := step(ctx); err != nil {
			return err
		}
	}
	d.report(ctx)
	return nil
}

func Ensure(ctx context.Context, c *cluster.Cluster, console *ui.Console) error {
	kind, managed := c.ManagedKind()
	if !managed {
		if err := c.Kube.Reachable(ctx); err != nil {
			return console.Errorf("%v", err)
		}
		console.OK("Using context %s", c.Kube.Context)
		return nil
	}
	state, err := kind.State(ctx)
	if err != nil {
		return console.Errorf("%v", err)
	}
	switch state {
	case kube.KindReady:
		console.OK("kind cluster %s exists and publishes 127.0.0.1:%d", kind.Name, kind.PublicPort)
		return nil
	case kube.KindStopped:
		if err := console.Run(ctx, "Start the stopped kind cluster "+kind.Name, func(ctx context.Context, w io.Writer) error {
			if err := kind.Start(ctx, w); err != nil {
				return err
			}
			return c.Kube.ClusterCmd("wait", "--for=condition=Ready", "node", "--all", "--timeout="+NodeTimeout.String()).Run(ctx, w)
		}); err != nil {
			return err
		}
		return nil
	case kube.KindMismatch:
		console.Warn("kind cluster %s does not publish the frontend on 127.0.0.1:%d", kind.Name, kind.PublicPort)
		ok, err := console.Confirm(fmt.Sprintf("Delete and recreate cluster %s? Everything in it is lost.", kind.Name), false)
		if err != nil {
			return err
		}
		if !ok {
			return console.Errorf("cannot publish the frontend without recreating the cluster")
		}
	}
	if !kube.PortFree(kind.PublicPort) && (state == kube.KindAbsent || !kind.HoldsPort(ctx, kind.PublicPort)) {
		return console.Errorf("host port %d is already in use", kind.PublicPort)
	}
	if state == kube.KindMismatch {
		if err := console.Run(ctx, "Delete cluster "+kind.Name, kind.Delete); err != nil {
			return err
		}
	}
	if err := console.Run(ctx, "Create cluster "+kind.Name, kind.Create); err != nil {
		return err
	}
	if state, err := kind.State(ctx); err != nil || state != kube.KindReady {
		return console.Errorf("cluster %s was created without the 127.0.0.1:%d mapping", kind.Name, kind.PublicPort)
	}
	return nil
}

func Loader(ctx context.Context, c *cluster.Cluster, console *ui.Console) (images.Loader, kube.Distro, error) {
	nodes, err := c.Kube.Nodes(ctx)
	if err != nil {
		return nil, "", console.Errorf("detecting the Kubernetes distribution: %v", err)
	}
	platform, err := images.Platform(ctx)
	if err != nil {
		return nil, "", console.Errorf("reading the Docker platform: %v", err)
	}
	distro := nodes.Distro
	if c.Config.Registry == "" && nodes.Count > 1 {
		return nil, "", console.Errorf("context %s has %d nodes, so images must come from a registry: set one in the profile", c.Kube.Context, nodes.Count)
	}
	var loader images.Loader
	switch {
	case c.Config.Registry != "":
		loader = images.RegistryLoader{Registry: c.Config.Registry}
	case distro == kube.DistroKind:
		kindName := nodes.KindName
		if kind, managed := c.ManagedKind(); managed {
			kindName = kind.Name
		}
		loader = images.KindLoader{Cluster: kube.Kind{Name: kindName}, Platform: platform}
	case distro == kube.DistroK3s:
		hostname, _ := os.Hostname()
		if !execx.Exists("k3s") || !strings.EqualFold(hostname, nodes.First) {
			return nil, "", console.Errorf("context %s is a k3s node (%s) on another machine: set a registry in the profile", c.Kube.Context, nodes.First)
		}
		if images.NeedsSudo() {
			console.Info("Importing into k3s needs sudo")
			if err := execx.Command("sudo", "-v").Interactive(ctx); err != nil {
				return nil, "", console.Errorf("sudo: %v", err)
			}
		}
		loader = images.K3sLoader{Platform: platform}
	default:
		return nil, "", console.Errorf("context %s runs %s, which can only get images from a registry: set one in the profile", c.Kube.Context, distro)
	}
	console.OK("Detected %s, images go to %s", distro, loader.Describe())
	return loader, distro, nil
}

func (d *Deployer) checkStorage(ctx context.Context) error {
	wanted, err := config.Mebibytes(d.Cluster.Config.PostgresStorage)
	if err != nil {
		return d.Console.Errorf("%v", err)
	}
	for _, service := range d.Cluster.LocalDatabases() {
		out, err := d.Cluster.Kube.Cmd("get", "statefulset", service+"-db", "--ignore-not-found", "-o", "jsonpath={.spec.volumeClaimTemplates[0].spec.resources.requests.storage}").Output(ctx)
		if err != nil {
			return d.Console.Errorf("reading %s-db: %v", service, err)
		}
		current := strings.TrimSpace(out)
		if current == "" {
			continue
		}
		existing, parseErr := config.Mebibytes(current)
		if parseErr != nil || existing != wanted {
			return d.Console.Errorf("%s-db already has %s of storage; postgres_storage %s cannot change once a database exists", service, current, d.Cluster.Config.PostgresStorage)
		}
	}
	return nil
}

func ThirdParty(k *config.K8s) []string {
	refs := []string{images.PostgresImage, images.RedpandaImage, images.FlywayImage, images.DindImage}
	if k.Grafana {
		refs = append(refs, images.GrafanaImage)
	}
	if k.Mail.Mode == config.MailMailpit {
		refs = append(refs, images.MailpitImage)
	}
	if k.ToolEnabled(config.ToolPgAdmin) {
		refs = append(refs, images.PgAdminImage)
	}
	if k.ToolEnabled(config.ToolKafkaUI) {
		refs = append(refs, images.KafkaUIImage)
	}
	return refs
}

func (d *Deployer) images(ctx context.Context) error {
	console := d.Console
	console.Section("Images")
	if d.loader.Preloads() {
		for _, image := range ThirdParty(d.Cluster.Config) {
			if d.loader.Has(ctx, image) {
				console.OK("%s already on the node", image)
				continue
			}
			if err := console.Run(ctx, "Pull "+image, func(ctx context.Context, w io.Writer) error { return images.Pull(ctx, w, image) }); err != nil {
				return err
			}
			if err := console.Run(ctx, "Load "+image, func(ctx context.Context, w io.Writer) error { return d.loader.Load(ctx, w, image) }); err != nil {
				return err
			}
		}
	}
	for _, image := range d.set.Own() {
		if err := console.Run(ctx, "Load "+image, func(ctx context.Context, w io.Writer) error { return d.loader.Load(ctx, w, image) }); err != nil {
			return err
		}
	}
	return nil
}

func (d *Deployer) namespace(ctx context.Context) error {
	d.Console.Section("Namespace")
	return d.Console.Run(ctx, "Apply namespace "+d.Cluster.Config.Namespace, func(ctx context.Context, w io.Writer) error {
		return d.Cluster.Apply(ctx, w, "namespace.yaml", d.vars, nil)
	})
}

func (d *Deployer) secrets(ctx context.Context) error {
	console := d.Console
	console.Section("Secrets")
	kc := d.Cluster.Kube
	changed := false
	for _, service := range d.Cluster.LocalDatabases() {
		database := d.Cluster.Config.Database(service)
		found, err := kc.Lookup(ctx, "secret", service+"-db-local")
		if err != nil {
			return console.Errorf("reading secret %s-db-local: %v", service, err)
		}
		if !found {
			continue
		}
		values, _, err := kc.Secret(ctx, service+"-db-local")
		if err != nil {
			return console.Errorf("reading secret %s-db-local: %v", service, err)
		}
		if values["password"] == "" {
			continue
		}
		if values["username"] != database.User || values["password"] != database.Password {
			database.User = values["username"]
			database.Password = values["password"]
			changed = true
			console.Info("Kept the existing %s-db credentials, Postgres only reads them on first start", service)
		}
	}
	if changed {
		if err := d.Store.Save(d.Cluster.Profile); err != nil {
			return console.Errorf("saving profile: %v", err)
		}
	}
	return console.Run(ctx, "Apply database, SMTP and ops secrets", func(ctx context.Context, w io.Writer) error {
		for _, service := range config.DBServices {
			database := d.Cluster.Config.Database(service)
			credentials := map[string]string{"username": database.User, "password": database.Password}
			if database.IsLocal() {
				if err := kc.ApplySecret(ctx, w, service+"-db-local", config.ModeLocal, credentials); err != nil {
					return err
				}
			}
			if err := kc.ApplySecret(ctx, w, service+"-db", database.Mode, credentials); err != nil {
				return err
			}
		}
		mail := d.Cluster.Config.Mail
		if mail.Authenticated() {
			if err := kc.ApplySecret(ctx, w, "notification-smtp", config.ModeExternal, map[string]string{"username": mail.User, "password": mail.Password}); err != nil {
				return err
			}
		} else if err := kc.Delete(ctx, w, "secret", "notification-smtp"); err != nil {
			return err
		}
		return d.applyOps(ctx, w)
	})
}

func (d *Deployer) infra(ctx context.Context) error {
	console := d.Console
	console.Section("Infrastructure")
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	err := console.Run(ctx, "Apply Postgres, Redpanda, Grafana, Mailpit and the admin tools", func(ctx context.Context, w io.Writer) error {
		for _, service := range config.DBServices {
			if k.Database(service).IsLocal() {
				if err := d.Cluster.Apply(ctx, w, "infra/postgres.yaml", d.vars, map[string]string{"DB_SERVICE": service, "DB_URL": ""}); err != nil {
					return err
				}
			} else if err := kc.Delete(ctx, w, "statefulset,service", service+"-db"); err != nil {
				return err
			}
		}
		if err := d.Cluster.Apply(ctx, w, "infra/redpanda.yaml", d.vars, nil); err != nil {
			return err
		}
		if k.Grafana {
			if err := d.Cluster.Apply(ctx, w, "infra/grafana.yaml", d.vars, nil); err != nil {
				return err
			}
		} else if err := kc.Delete(ctx, w, "deployment/grafana", "service/grafana"); err != nil {
			return err
		}
		if k.Mail.Mode == config.MailMailpit {
			if err := d.Cluster.Apply(ctx, w, "infra/mailpit.yaml", d.vars, nil); err != nil {
				return err
			}
		} else if err := kc.Delete(ctx, w, "deployment/mailpit", "service/mailpit"); err != nil {
			return err
		}
		return d.applyTools(ctx, w)
	})
	if err != nil {
		return err
	}
	var waits []rollout
	for _, service := range d.Cluster.LocalDatabases() {
		waits = append(waits, rollout{"statefulset/" + service + "-db", InfraTimeout})
	}
	waits = append(waits, rollout{"statefulset/redpanda", InfraTimeout})
	if k.Grafana {
		waits = append(waits, rollout{"deployment/grafana", GrafanaTimeout})
	}
	if k.Mail.Mode == config.MailMailpit {
		waits = append(waits, rollout{"deployment/mailpit", InfraTimeout})
	}
	waits = append(waits, d.toolWaits()...)
	for _, wait := range waits {
		if err := console.Run(ctx, "Wait for "+wait.resource, func(ctx context.Context, w io.Writer) error {
			return kc.Rollout(ctx, w, wait.resource, wait.timeout)
		}); err != nil {
			return err
		}
	}
	if k.Grafana {
		return console.Run(ctx, "Set the Grafana login to "+k.Ops.Email, d.syncGrafanaLogin)
	}
	return nil
}

func (d *Deployer) migrate(ctx context.Context) error {
	d.Console.Section("Migrations")
	for _, service := range config.DBServices {
		if err := d.Console.Run(ctx, "Migrate "+service+" database", func(ctx context.Context, w io.Writer) error {
			return d.Cluster.Migrate(ctx, w, d.vars, service, false)
		}); err != nil {
			return err
		}
	}
	return nil
}

func (d *Deployer) services(ctx context.Context) error {
	console := d.Console
	console.Section("Services")
	for _, service := range images.Services {
		overrides := map[string]string{"IMAGE_TAG": d.set.Tag(service)}
		if err := console.Run(ctx, "Apply "+service+" "+d.set.Tag(service), func(ctx context.Context, w io.Writer) error {
			return d.Cluster.Apply(ctx, w, "services/"+service+".yaml", d.vars, overrides)
		}); err != nil {
			return err
		}
	}
	for _, service := range images.Services {
		if err := console.Run(ctx, "Wait for "+service, func(ctx context.Context, w io.Writer) error {
			return d.Cluster.Kube.Rollout(ctx, w, "deployment/"+service, ServiceTimeout)
		}); err != nil {
			return err
		}
	}
	return nil
}

func (d *Deployer) installController(ctx context.Context, w io.Writer) error {
	kc := d.Cluster.Kube
	installed, _ := kc.ClusterCmd("-n", EnvoyNamespace, "get", "deployment", "envoy-gateway", "-o", "jsonpath={.spec.template.spec.containers[0].image}").Output(ctx)
	if strings.TrimSpace(installed) == "envoyproxy/gateway:"+EnvoyGatewayVersion {
		fmt.Fprintf(w, "Envoy Gateway %s is already installed\n", EnvoyGatewayVersion)
		return nil
	}
	bundle, err := download(ctx, "https://github.com/envoyproxy/gateway/releases/download/"+EnvoyGatewayVersion+"/install.yaml")
	if err != nil {
		return err
	}
	if err := kc.ClusterCmd("-n", EnvoyNamespace, "delete", "job", EnvoyCertgenJob, "--ignore-not-found", "--wait=true").Run(ctx, w); err != nil {
		return err
	}
	if err := kc.ApplyCluster(ctx, w, bundle); err != nil {
		return err
	}
	crds := append([]string{"wait", "--for=condition=Established"}, GatewayCRDs...)
	if err := kc.ClusterCmd(append(crds, "--timeout="+GatewayTimeout.String())...).Run(ctx, w); err != nil {
		return err
	}
	return kc.ClusterCmd("-n", EnvoyNamespace, "rollout", "status", "deployment/envoy-gateway", "--timeout="+GatewayTimeout.String()).Run(ctx, w)
}

func download(ctx context.Context, url string) (string, error) {
	ctx, cancel := context.WithTimeout(ctx, BundleTimeout)
	defer cancel()
	var lastErr error
	for attempt := 0; attempt < DownloadAttempts; attempt++ {
		if attempt > 0 {
			select {
			case <-ctx.Done():
				return "", ctx.Err()
			case <-time.After(time.Duration(attempt) * DownloadBackoff):
			}
		}
		request, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
		if err != nil {
			return "", err
		}
		response, err := http.DefaultClient.Do(request)
		if err != nil {
			lastErr = err
			continue
		}
		body, err := io.ReadAll(io.LimitReader(response.Body, MaxBundleBytes))
		response.Body.Close()
		if err != nil {
			lastErr = err
			continue
		}
		if response.StatusCode != http.StatusOK {
			lastErr = fmt.Errorf("GET %s: %s", url, response.Status)
			if response.StatusCode < http.StatusInternalServerError {
				return "", lastErr
			}
			continue
		}
		return string(body), nil
	}
	return "", lastErr
}

func (d *Deployer) waitRoute(ctx context.Context, route string) error {
	deadline := time.Now().Add(RouteTimeout)
	var status string
	for time.Now().Before(deadline) {
		out, _ := d.Cluster.Kube.Cmd("get", "httproute", route, "-o", "jsonpath={range .status.parents[*].conditions[*]}{.type}={.status} {end}").Output(ctx)
		status = " " + out + " "
		if strings.Contains(status, " Accepted=True ") && strings.Contains(status, " ResolvedRefs=True ") {
			return nil
		}
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-time.After(RoutePoll):
		}
	}
	return errors.New("HTTPRoute " + route + " is not ready:" + status)
}
