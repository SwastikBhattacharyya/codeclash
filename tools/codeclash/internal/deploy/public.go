package deploy

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"slices"
	"strconv"
	"strings"
	"time"
	"unicode"

	"codeclash/internal/config"
	"codeclash/internal/kube"
)

const (
	GatewayName          = "codeclash"
	HTTPListener         = "http"
	HTTPSListenerPrefix  = "https-"
	WebKey               = "web"
	ToolRoutePrefix      = "tool-"
	RedirectRoute        = "https-redirect"
	RouteLabel           = "codeclash.dev/route"
	OpsSecret            = "ops"
	PgAdminServers       = "pgadmin-servers"
	PgAdminPgpass        = "pgadmin-pgpass"
	PgAdminStorageRoot   = "/var/lib/pgadmin/storage/"
	PgAdminUserPrefix    = "pga_user_"
	PgAdminGroup         = "CodeClash"
	PgAdminPassFile      = "/pgpass"
	DefaultSSLMode       = "prefer"
	CertManagerVersion   = "v1.21.3"
	CertManagerNamespace = "cert-manager"
	CertManagerFlag      = "--enable-gateway-api"
	CertTimeout          = 5 * time.Minute
	IssuerRetries        = 10
	IssuerRetryDelay     = 6 * time.Second
	RedirectStatus       = 301
	GrafanaHome          = "/otel-lgtm/grafana"
	GrafanaDataPath      = "/data/grafana/data"
	GrafanaAdminID       = 1
	GrafanaAdminName     = "ops"
	GrafanaDefaultLogin  = "admin"
	GrafanaLoginMap      = "grafana-login"
	TraefikNamespace     = "kube-system"
	TraefikService       = "traefik"
	HashLength           = 16
	ServiceLoadBalancer  = "LoadBalancer"
	ServiceNodePort      = "NodePort"
	ServiceClusterIP     = "ClusterIP"
	HTTPSScheme          = "https"
	EnvoyReplicas        = 1
	EnvoyMemoryRequest   = "64Mi"
	EnvoyMemoryLimit     = "256Mi"
	RouteLabelValue      = "true"
	UnknownAddress       = "<server public IP>"
)

var (
	CertManagerDeployments = []string{"cert-manager", "cert-manager-cainjector", "cert-manager-webhook"}
	ACMEServers            = map[string]string{
		config.IssuerStaging:    "https://acme-staging-v02.api.letsencrypt.org/directory",
		config.IssuerProduction: "https://acme-v02.api.letsencrypt.org/directory",
	}
)

type tool struct {
	service string
	port    int
	label   string
}

var tools = map[string]tool{
	config.ToolGrafana: {"grafana", GrafanaPort, "Grafana"},
	config.ToolPgAdmin: {"pgadmin", ServicePort, "pgAdmin"},
	config.ToolKafkaUI: {"kafka-ui", ServicePort, "Kafka UI"},
	config.ToolMailpit: {"mailpit", MailpitUIPort, "Mailpit"},
}

func digest(parts ...string) string {
	sum := sha256.Sum256([]byte(strings.Join(parts, "\x00")))
	return hex.EncodeToString(sum[:])[:HashLength]
}

func PgAdminStorageDir(email string) string {
	name := email
	if name == "" || unicode.IsDigit(rune(name[0])) {
		name = PgAdminUserPrefix + name
	}
	name = strings.NewReplacer("@", "_", "/", "slash", "\\", "slash").Replace(name)
	return PgAdminStorageRoot + name
}

func (d *Deployer) publicVars() {
	k := d.Cluster.Config
	d.vars["GRAFANA_ROOT_URL"] = k.URL(config.ToolGrafana) + "/"
	d.vars["PGADMIN_STORAGE_DIR"] = PgAdminStorageDir(k.Ops.Email)
	d.vars["OPS_HASH"] = digest(k.Ops.Email, k.Ops.Password)
}

type pgServer struct {
	Name          string `json:"Name"`
	Group         string `json:"Group"`
	Host          string `json:"Host"`
	Port          int    `json:"Port"`
	MaintenanceDB string `json:"MaintenanceDB"`
	Username      string `json:"Username"`
	SSLMode       string `json:"SSLMode"`
	PassFile      string `json:"PassFile"`
}

func (d *Deployer) pgAdminConfig() (string, string, error) {
	servers := map[string]pgServer{}
	var pgpass strings.Builder
	escape := strings.NewReplacer(`\`, `\\`, `:`, `\:`)
	for index, service := range config.DBServices {
		database := d.Cluster.Config.Database(service)
		conn, err := database.LibpqURL()
		if err != nil {
			return "", "", err
		}
		host, port, sslMode, err := endpoint(conn)
		if err != nil {
			return "", "", err
		}
		servers[strconv.Itoa(index+1)] = pgServer{
			Name: service, Group: PgAdminGroup, Host: host, Port: port, MaintenanceDB: database.Name(),
			Username: database.User, SSLMode: sslMode, PassFile: PgAdminPassFile,
		}
		fmt.Fprintf(&pgpass, "%s:%d:*:%s:%s\n", escape.Replace(host), port, escape.Replace(database.User), escape.Replace(database.Password))
	}
	data, err := json.MarshalIndent(map[string]any{"Servers": servers}, "", "  ")
	if err != nil {
		return "", "", err
	}
	return string(data), pgpass.String(), nil
}

func endpoint(conn string) (string, int, string, error) {
	rest := strings.TrimPrefix(conn, "postgresql://")
	hostPort, query, _ := strings.Cut(rest, "?")
	hostPort, _, _ = strings.Cut(hostPort, "/")
	host, portText, found := strings.Cut(hostPort, ":")
	port := config.PostgresPort
	if found {
		parsed, err := strconv.Atoi(portText)
		if err != nil {
			return "", 0, "", fmt.Errorf("invalid port in %s", conn)
		}
		port = parsed
	}
	sslMode := DefaultSSLMode
	for _, pair := range strings.Split(query, "&") {
		if value, ok := strings.CutPrefix(pair, "sslmode="); ok {
			sslMode = value
		}
	}
	return host, port, sslMode, nil
}

func (d *Deployer) applyOps(ctx context.Context, w io.Writer) error {
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	ops := map[string]string{"username": k.Ops.Email, "password": k.Ops.Password, "ui-auth": k.Ops.Email + ":" + k.Ops.Password}
	if err := kc.ApplySecret(ctx, w, OpsSecret, config.ModeLocal, ops); err != nil {
		return err
	}
	if !k.ToolEnabled(config.ToolPgAdmin) {
		if err := kc.Delete(ctx, w, "secret", PgAdminPgpass); err != nil {
			return err
		}
		return kc.Delete(ctx, w, "configmap", PgAdminServers)
	}
	servers, pgpass, err := d.pgAdminConfig()
	if err != nil {
		return err
	}
	d.vars["PGADMIN_CONFIG_HASH"] = digest(servers, pgpass, k.Ops.Email, k.Ops.Password)
	if err := kc.ApplySecret(ctx, w, PgAdminPgpass, config.ModeLocal, map[string]string{"pgpass": pgpass}); err != nil {
		return err
	}
	return kc.ApplyConfigMap(ctx, w, PgAdminServers, map[string]string{"servers.json": servers})
}

func (d *Deployer) applyTools(ctx context.Context, w io.Writer) error {
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	for _, item := range []struct {
		tool string
		file string
		name string
	}{{config.ToolPgAdmin, "infra/pgadmin.yaml", "pgadmin"}, {config.ToolKafkaUI, "infra/kafka-ui.yaml", "kafka-ui"}} {
		if k.ToolEnabled(item.tool) {
			if err := d.Cluster.Apply(ctx, w, item.file, d.vars, nil); err != nil {
				return err
			}
		} else if err := kc.Delete(ctx, w, "deployment/"+item.name, "service/"+item.name); err != nil {
			return err
		}
	}
	return nil
}

func (d *Deployer) toolWaits() []rollout {
	var waits []rollout
	for _, item := range []struct{ tool, name string }{{config.ToolPgAdmin, "pgadmin"}, {config.ToolKafkaUI, "kafka-ui"}} {
		if d.Cluster.Config.ToolEnabled(item.tool) {
			waits = append(waits, rollout{"deployment/" + item.name, InfraTimeout})
		}
	}
	return waits
}

const grafanaSync = `set -e
IFS= read -r password
IFS= read -r body
cd ` + GrafanaHome + ` && GF_PATHS_DATA=` + GrafanaDataPath + ` ./bin/grafana cli --homepath ` + GrafanaHome + ` admin reset-admin-password "$password" >/dev/null
port=$1
id=$2
shift 2
for login in "$@"; do
  if curl -sSf -o /dev/null -X PUT -H 'Content-Type: application/json' -u "$login:$password" --data-binary "$body" "http://localhost:$port/api/users/$id"; then
    exit 0
  fi
done
echo "could not rename the Grafana admin with any of: $*" >&2
exit 1
`

func (d *Deployer) syncGrafanaLogin(ctx context.Context, w io.Writer) error {
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	previous := k.Ops.Email
	out, err := kc.Cmd("get", "configmap", GrafanaLoginMap, "--ignore-not-found", "-o", "jsonpath={.data.login}").Output(ctx)
	if err != nil {
		return err
	}
	if login := strings.TrimSpace(out); login != "" {
		previous = login
	}
	body, err := json.Marshal(map[string]string{"login": k.Ops.Email, "email": k.Ops.Email, "name": GrafanaAdminName})
	if err != nil {
		return err
	}
	stdin := k.Ops.Password + "\n" + string(body) + "\n"
	args := []string{"exec", "-i", "deployment/grafana", "--", "sh", "-c", grafanaSync, "sh", strconv.Itoa(GrafanaPort), strconv.Itoa(GrafanaAdminID), previous, k.Ops.Email, GrafanaDefaultLogin}
	if err := kc.Cmd(args...).StdinString(stdin).Run(ctx, w); err != nil {
		return err
	}
	return kc.ApplyConfigMap(ctx, w, GrafanaLoginMap, map[string]string{"login": k.Ops.Email})
}

func (d *Deployer) checkTraefik(ctx context.Context, distro kube.Distro) error {
	k := d.Cluster.Config
	if distro != kube.DistroK3s || k.IsKind() {
		return nil
	}
	traefikPorts := []int{config.DefaultHTTPPort, config.DefaultHTTPSPort}
	if !slices.Contains(traefikPorts, k.HTTPPort) && !slices.Contains(traefikPorts, k.HTTPSPort) {
		return nil
	}
	probe := kube.Kube{Context: d.Cluster.Kube.Context, Namespace: TraefikNamespace}
	found, err := probe.Lookup(ctx, "service", TraefikService)
	if err != nil {
		return d.Console.Errorf("checking for Traefik: %v", err)
	}
	if found {
		return d.Console.Errorf("k3s's bundled Traefik holds ports 80/443. Add\n      disable:\n        - traefik\n    to /etc/rancher/k3s/config.yaml, run `sudo systemctl restart k3s`, wait for Traefik to disappear from kube-system, then deploy again")
	}
	return nil
}

type host struct {
	key      string
	hostname string
}

func (d *Deployer) hosts() []host {
	k := d.Cluster.Config
	hosts := []host{{WebKey, k.Host("")}}
	for _, name := range k.EnabledTools() {
		hosts = append(hosts, host{name, k.Host(name)})
	}
	return hosts
}

func meta(name string, namespace string, labels map[string]string) map[string]any {
	all := map[string]string{kube.PartOfLabel: kube.PartOfValue}
	for key, value := range labels {
		all[key] = value
	}
	object := map[string]any{"name": name, "labels": all}
	if namespace != "" {
		object["namespace"] = namespace
	}
	return object
}

func (d *Deployer) envoyProxy() map[string]any {
	k := d.Cluster.Config
	service := map[string]any{"type": ServiceLoadBalancer}
	if k.IsKind() {
		service = map[string]any{
			"type": ServiceNodePort,
			"patch": map[string]any{
				"type":  "StrategicMerge",
				"value": map[string]any{"spec": map[string]any{"ports": []map[string]any{{"port": config.DefaultHTTPPort, "nodePort": kube.GatewayNodePort}}}},
			},
		}
	}
	return map[string]any{
		"apiVersion": "gateway.envoyproxy.io/v1alpha1",
		"kind":       "EnvoyProxy",
		"metadata":   meta(GatewayName, k.Namespace, nil),
		"spec": map[string]any{
			"provider": map[string]any{
				"type": "Kubernetes",
				"kubernetes": map[string]any{
					"envoyService": service,
					"envoyDeployment": map[string]any{
						"replicas": EnvoyReplicas,
						"container": map[string]any{"resources": map[string]any{
							"requests": map[string]string{"memory": EnvoyMemoryRequest},
							"limits":   map[string]string{"memory": EnvoyMemoryLimit},
						}},
					},
				},
			},
		},
	}
}

func (d *Deployer) gatewayClass() map[string]any {
	return map[string]any{
		"apiVersion": "gateway.networking.k8s.io/v1",
		"kind":       "GatewayClass",
		"metadata":   meta(GatewayName, "", nil),
		"spec": map[string]any{
			"controllerName": "gateway.envoyproxy.io/gatewayclass-controller",
			"parametersRef": map[string]any{
				"group": "gateway.envoyproxy.io", "kind": "EnvoyProxy", "name": GatewayName, "namespace": d.Cluster.Config.Namespace,
			},
		},
	}
}

func (d *Deployer) gatewayObject() map[string]any {
	k := d.Cluster.Config
	httpPort := k.HTTPPort
	if k.IsKind() {
		httpPort = config.DefaultHTTPPort
	}
	listeners := []map[string]any{{
		"name": HTTPListener, "protocol": "HTTP", "port": httpPort,
		"allowedRoutes": map[string]any{"namespaces": map[string]string{"from": "Same"}},
	}}
	if k.TLSActive() {
		for _, h := range d.hosts() {
			listeners = append(listeners, map[string]any{
				"name": HTTPSListenerPrefix + h.key, "protocol": "HTTPS", "port": k.HTTPSPort, "hostname": h.hostname,
				"tls":           map[string]any{"mode": "Terminate", "certificateRefs": []map[string]string{{"name": h.key + "-tls"}}},
				"allowedRoutes": map[string]any{"namespaces": map[string]string{"from": "Same"}},
			})
		}
	}
	return map[string]any{
		"apiVersion": "gateway.networking.k8s.io/v1",
		"kind":       "Gateway",
		"metadata":   meta(GatewayName, k.Namespace, nil),
		"spec":       map[string]any{"gatewayClassName": GatewayName, "listeners": listeners},
	}
}

func (d *Deployer) parent(key string) []map[string]any {
	ref := map[string]any{"name": GatewayName}
	if d.Cluster.Config.TLSActive() {
		ref["sectionName"] = HTTPSListenerPrefix + key
	}
	return []map[string]any{ref}
}

func (d *Deployer) route(name string, key string, hostnames []string, rule map[string]any) map[string]any {
	spec := map[string]any{"parentRefs": d.parent(key), "rules": []map[string]any{rule}}
	if len(hostnames) > 0 {
		spec["hostnames"] = hostnames
	}
	return map[string]any{
		"apiVersion": "gateway.networking.k8s.io/v1",
		"kind":       "HTTPRoute",
		"metadata":   meta(name, d.Cluster.Config.Namespace, map[string]string{kube.NameLabel: name, RouteLabel: RouteLabelValue}),
		"spec":       spec,
	}
}

func prefixMatch() []map[string]any {
	return []map[string]any{{"path": map[string]string{"type": "PathPrefix", "value": "/"}}}
}

func (d *Deployer) routes() []map[string]any {
	k := d.Cluster.Config
	var webHosts []string
	if k.Domain != config.LocalDomain {
		webHosts = []string{k.Host("")}
	}
	routes := []map[string]any{d.route(WebKey, WebKey, webHosts, map[string]any{
		"matches": prefixMatch(),
		"filters": []map[string]any{{
			"type":                  "RequestHeaderModifier",
			"requestHeaderModifier": map[string]any{"remove": []string{"X-Forwarded-Host"}},
		}},
		"backendRefs": []map[string]any{{"name": "web", "port": ServicePort}},
	})}
	for _, name := range k.EnabledTools() {
		spec := tools[name]
		routes = append(routes, d.route(ToolRoutePrefix+name, name, []string{k.Host(name)}, map[string]any{
			"matches":     prefixMatch(),
			"backendRefs": []map[string]any{{"name": spec.service, "port": spec.port}},
		}))
	}
	if k.TLSActive() {
		redirect := map[string]any{"scheme": HTTPSScheme, "statusCode": RedirectStatus}
		if k.HTTPSPort != config.DefaultHTTPSPort {
			redirect["port"] = k.HTTPSPort
		}
		routes = append(routes, map[string]any{
			"apiVersion": "gateway.networking.k8s.io/v1",
			"kind":       "HTTPRoute",
			"metadata":   meta(RedirectRoute, k.Namespace, map[string]string{kube.NameLabel: RedirectRoute, RouteLabel: RouteLabelValue}),
			"spec": map[string]any{
				"parentRefs": []map[string]any{{"name": GatewayName, "sectionName": HTTPListener}},
				"rules":      []map[string]any{{"matches": prefixMatch(), "filters": []map[string]any{{"type": "RequestRedirect", "requestRedirect": redirect}}}},
			},
		})
	}
	return routes
}

func routeName(route map[string]any) string {
	return route["metadata"].(map[string]any)["name"].(string)
}

func (d *Deployer) issuerName() string {
	return "codeclash-" + d.Cluster.Config.TLS.Issuer
}

func (d *Deployer) clusterIssuer() map[string]any {
	k := d.Cluster.Config
	return map[string]any{
		"apiVersion": "cert-manager.io/v1",
		"kind":       "ClusterIssuer",
		"metadata":   meta(d.issuerName(), "", nil),
		"spec": map[string]any{
			"acme": map[string]any{
				"email":               k.TLS.Email,
				"server":              ACMEServers[k.TLS.Issuer],
				"privateKeySecretRef": map[string]string{"name": d.issuerName() + "-account"},
				"solvers": []map[string]any{{
					"http01": map[string]any{"gatewayHTTPRoute": map[string]any{
						"serviceType": ServiceClusterIP,
						"parentRefs":  []map[string]any{{"name": GatewayName, "namespace": k.Namespace, "kind": "Gateway", "sectionName": HTTPListener}},
					}},
				}},
			},
		},
	}
}

func (d *Deployer) certificate(h host) map[string]any {
	return map[string]any{
		"apiVersion": "cert-manager.io/v1",
		"kind":       "Certificate",
		"metadata":   meta(h.key+"-tls", d.Cluster.Config.Namespace, nil),
		"spec": map[string]any{
			"secretName": h.key + "-tls",
			"dnsNames":   []string{h.hostname},
			"issuerRef":  map[string]string{"kind": "ClusterIssuer", "name": d.issuerName()},
		},
	}
}

func (d *Deployer) installCertManager(ctx context.Context, w io.Writer) error {
	kc := d.Cluster.Kube
	controller := "quay.io/jetstack/cert-manager-controller:" + CertManagerVersion
	installed, _ := kc.ClusterCmd("-n", CertManagerNamespace, "get", "deployment", "cert-manager", "-o", "jsonpath={.spec.template.spec.containers[0].image}").Output(ctx)
	if strings.TrimSpace(installed) != controller {
		bundle, err := download(ctx, "https://github.com/cert-manager/cert-manager/releases/download/"+CertManagerVersion+"/cert-manager.yaml")
		if err != nil {
			return err
		}
		if err := kc.ApplyCluster(ctx, w, bundle); err != nil {
			return err
		}
	}
	args, err := kc.ClusterCmd("-n", CertManagerNamespace, "get", "deployment", "cert-manager", "-o", "jsonpath={.spec.template.spec.containers[0].args}").Output(ctx)
	if err != nil {
		return err
	}
	var current []string
	if err := json.Unmarshal([]byte(strings.TrimSpace(args)), &current); err != nil {
		return fmt.Errorf("reading cert-manager args: %w", err)
	}
	if !slices.Contains(current, CertManagerFlag) {
		patch := `[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"` + CertManagerFlag + `"}]`
		if err := kc.ClusterCmd("-n", CertManagerNamespace, "patch", "deployment", "cert-manager", "--type=json", "-p", patch).Run(ctx, w); err != nil {
			return err
		}
	}
	for _, deployment := range CertManagerDeployments {
		if err := kc.ClusterCmd("-n", CertManagerNamespace, "rollout", "status", "deployment/"+deployment, "--timeout="+GatewayTimeout.String()).Run(ctx, w); err != nil {
			return err
		}
	}
	issuer, err := json.Marshal(d.clusterIssuer())
	if err != nil {
		return err
	}
	for attempt := 1; ; attempt++ {
		err := kc.ApplyCluster(ctx, w, string(issuer))
		if err == nil || attempt == IssuerRetries {
			return err
		}
		fmt.Fprintln(w, "cert-manager webhook not ready yet, retrying")
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-time.After(IssuerRetryDelay):
		}
	}
}

func (d *Deployer) gateway(ctx context.Context) error {
	console := d.Console
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	console.Section("Public access")
	if err := console.Run(ctx, "Install Envoy Gateway "+EnvoyGatewayVersion, d.installController); err != nil {
		return err
	}
	if k.TLSActive() {
		if err := console.Run(ctx, "Install cert-manager "+CertManagerVersion+" and the "+k.TLS.Issuer+" issuer", d.installCertManager); err != nil {
			return err
		}
	}
	routes := d.routes()
	if err := console.Run(ctx, "Apply the gateway and routes", func(ctx context.Context, w io.Writer) error {
		for _, object := range []map[string]any{d.envoyProxy(), d.gatewayClass(), d.gatewayObject()} {
			if err := kc.ApplyObject(ctx, w, object); err != nil {
				return err
			}
		}
		if err := d.Cluster.Apply(ctx, w, "gateway/client-traffic.yaml", d.vars, nil); err != nil {
			return err
		}
		keep := map[string]bool{}
		for _, route := range routes {
			if err := kc.ApplyObject(ctx, w, route); err != nil {
				return err
			}
			keep[routeName(route)] = true
		}
		out, err := kc.Cmd("get", "httproute", "--selector", RouteLabel+"="+RouteLabelValue, "-o", "jsonpath={.items[*].metadata.name}").Output(ctx)
		if err != nil {
			return err
		}
		for _, name := range strings.Fields(out) {
			if !keep[name] {
				if err := kc.Delete(ctx, w, "httproute", name); err != nil {
					return err
				}
			}
		}
		if err := d.syncCertificates(ctx, w); err != nil {
			return err
		}
		if err := kc.Cmd("wait", "--for=condition=Programmed", "gateway/"+GatewayName, "--timeout="+GatewayTimeout.String()).Run(ctx, w); err != nil {
			return err
		}
		for _, route := range routes {
			if err := d.waitRoute(ctx, routeName(route)); err != nil {
				return err
			}
		}
		return nil
	}); err != nil {
		return err
	}
	if !k.TLSActive() {
		return nil
	}
	d.dnsTable(ctx)
	var names []string
	for _, h := range d.hosts() {
		names = append(names, "certificate/"+h.key+"-tls")
	}
	if err := console.Run(ctx, "Issue TLS certificates", func(ctx context.Context, w io.Writer) error {
		args := append([]string{"wait", "--for=condition=Ready"}, names...)
		return kc.Cmd(append(args, "--timeout="+CertTimeout.String())...).Run(ctx, w)
	}); err != nil {
		console.Warn("Certificates are not ready yet. Check that the DNS records above point at this server; cert-manager keeps retrying. Watch: kubectl --context %s -n %s get certificates", kc.Context, k.Namespace)
	}
	return nil
}

func (d *Deployer) syncCertificates(ctx context.Context, w io.Writer) error {
	k := d.Cluster.Config
	kc := d.Cluster.Kube
	keep := map[string]bool{}
	if k.TLSActive() {
		for _, h := range d.hosts() {
			if err := kc.ApplyObject(ctx, w, d.certificate(h)); err != nil {
				return err
			}
			keep[h.key+"-tls"] = true
		}
	}
	out, err := kc.Cmd("get", "certificates.cert-manager.io", "--selector", kube.PartOfLabel+"="+kube.PartOfValue, "-o", "jsonpath={.items[*].metadata.name}").Output(ctx)
	if err != nil {
		if k.TLSActive() {
			return err
		}
		return nil
	}
	for _, name := range strings.Fields(out) {
		if keep[name] {
			continue
		}
		if err := kc.Delete(ctx, w, "certificates.cert-manager.io/"+name, "secret/"+name); err != nil {
			return err
		}
	}
	return nil
}

func (d *Deployer) dnsTable(ctx context.Context) {
	k := d.Cluster.Config
	if !strings.Contains(k.Domain, ".") {
		return
	}
	address := d.gatewayAddress(ctx)
	d.Console.Section("DNS")
	d.Console.Table([]string{"Type", "Name", "Value"}, [][]string{
		{"A", k.Domain, address},
		{"A", "*." + k.Domain, address},
	})
}

func (d *Deployer) report(ctx context.Context) {
	console := d.Console
	k := d.Cluster.Config
	console.Section("Done")
	rows := [][]string{{"Frontend", k.URL("")}}
	for _, name := range k.EnabledTools() {
		rows = append(rows, []string{tools[name].label, k.URL(name)})
	}
	console.Table([]string{"What", "URL"}, rows)
	if len(k.EnabledTools()) > 0 {
		console.Info("Tools log in as %s with ops.password from the profile", k.Ops.Email)
	}
	if !k.IsKind() && strings.Contains(k.Domain, ".") && !k.TLSActive() {
		console.Warn("TLS is off: logins to the tools cross the network unencrypted")
	}
	if !k.TLSActive() {
		d.dnsTable(ctx)
	}
	prefix := "kubectl --context " + d.Cluster.Kube.Context + " -n " + k.Namespace
	console.Info("Swagger UI: %s port-forward svc/gateway-service %d:%d", prefix, GatewayLocalPort, ServicePort)
}

func (d *Deployer) gatewayAddress(ctx context.Context) string {
	out, err := d.Cluster.Kube.Cmd("get", "gateway/"+GatewayName, "-o", "jsonpath={.status.addresses[0].value}").Output(ctx)
	if err == nil && strings.TrimSpace(out) != "" {
		return strings.TrimSpace(out)
	}
	return UnknownAddress
}
