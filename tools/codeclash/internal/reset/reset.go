package reset

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/cluster"
	"codeclash/internal/compose"
	"codeclash/internal/config"
	"codeclash/internal/execx"
	"codeclash/internal/images"
	"codeclash/internal/pg"
	"codeclash/internal/ui"
)

const (
	ScaleTimeout    = 3 * time.Minute
	RolloutTimeout  = 10 * time.Minute
	MailpitUIPort   = 8025
	DefaultReplicas = 1
	GroupDeleteWait = 90 * time.Second
	GroupDeletePoll = 5 * time.Second
	HTTPTimeout     = 15 * time.Second
	messagesPath    = "/api/v1/messages"
)

type rpk func(args ...string) *execx.Cmd

func clearKafka(ctx context.Context, w io.Writer, run rpk) error {
	out, err := run("topic", "list").Output(ctx)
	if err != nil {
		return err
	}
	var topics []string
	for _, line := range dataLines(out) {
		if fields := strings.Fields(line); len(fields) > 0 && !strings.HasPrefix(fields[0], "_") {
			topics = append(topics, fields[0])
		}
	}
	if len(topics) > 0 {
		if err := run(append([]string{"topic", "delete"}, topics...)...).Run(ctx, w); err != nil {
			return err
		}
	}
	fmt.Fprintf(w, "deleted %d topic(s)\n", len(topics))
	out, err = run("group", "list").Output(ctx)
	if err != nil {
		return err
	}
	var groups []string
	for _, line := range dataLines(out) {
		if fields := strings.Fields(line); len(fields) > 1 {
			groups = append(groups, fields[1])
		}
	}
	if len(groups) > 0 {
		deadline := time.Now().Add(GroupDeleteWait)
		for {
			err := run(append([]string{"group", "delete"}, groups...)...).Run(ctx, w)
			if err == nil {
				break
			}
			if time.Now().After(deadline) {
				return err
			}
			fmt.Fprintln(w, "consumer groups still have members, retrying")
			select {
			case <-ctx.Done():
				return ctx.Err()
			case <-time.After(GroupDeletePoll):
			}
		}
	}
	fmt.Fprintf(w, "deleted %d consumer group(s)\n", len(groups))
	return nil
}

func dataLines(out string) []string {
	lines := strings.Split(strings.TrimSpace(out), "\n")
	if len(lines) <= 1 {
		return nil
	}
	return lines[1:]
}

func clearMailpit(ctx context.Context, base string, user string, password string) error {
	ctx, cancel := context.WithTimeout(ctx, HTTPTimeout)
	defer cancel()
	request, err := http.NewRequestWithContext(ctx, http.MethodDelete, base+messagesPath, nil)
	if err != nil {
		return err
	}
	if user != "" {
		request.SetBasicAuth(user, password)
	}
	response, err := http.DefaultClient.Do(request)
	if err != nil {
		return fmt.Errorf("cannot reach Mailpit at %s: %w", base, err)
	}
	response.Body.Close()
	if response.StatusCode/100 != 2 {
		return fmt.Errorf("Mailpit answered %s", response.Status)
	}
	return nil
}

func Compose(ctx context.Context, console *ui.Console, c *compose.Compose) error {
	console.Section("Reset the compose stack")
	running, err := c.Running(ctx)
	if err != nil {
		return console.Errorf("docker compose: %v", err)
	}
	for _, service := range []string{compose.DB, compose.Redpanda} {
		if !running[service] {
			return console.Errorf("the %s container is not running, start the infrastructure first", service)
		}
	}
	postgres, err := pg.NewCompose(c)
	if err != nil {
		return console.Errorf("%v", err)
	}
	connected, err := postgres.Query(ctx, "postgres", "select count(*) from pg_stat_activity where datname = any(string_to_array(:'databases', ',')) and pid <> pg_backend_pid();\n",
		map[string]string{"databases": strings.Join(config.DBServices, ",")})
	if err != nil {
		return console.Errorf("checking database connections: %v", err)
	}
	if count, _ := strconv.Atoi(strings.TrimSpace(connected)); count > 0 {
		return console.Errorf("%d connection(s) are open to the service databases, stop the services first", count)
	}
	ok, err := console.Confirm("Wipe every service database, all Kafka topics and consumer groups, and all mail?", false)
	if err != nil || !ok {
		return err
	}
	var tasks []string
	for _, service := range config.DBServices {
		tasks = append(tasks, ":app:"+service+"-service:flywayClean", ":app:"+service+"-service:flywayMigrate")
	}
	var env []string
	for key, value := range c.Env {
		env = append(env, key+"="+value)
	}
	if err := console.Run(ctx, "Clean and migrate the databases", func(ctx context.Context, w io.Writer) error {
		return execx.Command(c.Repo.Path("gradlew"), append([]string{"-q", "--console=plain"}, tasks...)...).Dir(c.Repo.Root).Env(env...).Run(ctx, w)
	}); err != nil {
		return err
	}
	if err := console.Run(ctx, "Delete Kafka topics and consumer groups", func(ctx context.Context, w io.Writer) error {
		return clearKafka(ctx, w, func(args ...string) *execx.Cmd {
			return c.Exec(compose.Redpanda, append([]string{"rpk"}, args...)...)
		})
	}); err != nil {
		return err
	}
	if running[compose.Mailpit] {
		base, err := c.URL("MAIL_UI_PORT")
		if err != nil {
			return console.Errorf("%v", err)
		}
		if err := console.Run(ctx, "Delete Mailpit messages", func(ctx context.Context, w io.Writer) error {
			return clearMailpit(ctx, base, "", "")
		}); err != nil {
			return err
		}
	} else {
		console.Info("Mailpit is not running, skipped")
	}
	console.Info("Start auth-service once so the default roles sync before seeding the superadmin")
	return nil
}

func K8s(ctx context.Context, console *ui.Console, c *cluster.Cluster) error {
	console.Section("Reset " + c.Kube.Context + "/" + c.Kube.Namespace)
	kc := c.Kube
	if !kc.NamespaceExists(ctx) {
		console.Info("Namespace %s does not exist, nothing to reset", c.Kube.Namespace)
		return nil
	}
	var databases []string
	for _, service := range config.DBServices {
		database := c.Config.Database(service)
		if database.IsLocal() {
			if kc.Exists(ctx, "statefulset", service+"-db") {
				databases = append(databases, service)
			}
			continue
		}
		ok, err := console.ConfirmTyped(fmt.Sprintf("Reset the EXTERNAL %s database at %s?", service, database.URL), database.Name())
		if err != nil {
			return err
		}
		if ok {
			databases = append(databases, service)
		} else {
			console.Warn("Skipping the external %s database", service)
		}
	}
	ok, err := console.Confirm(fmt.Sprintf("Wipe %d database(s) (%s), all Kafka topics and consumer groups, and all mail?", len(databases), strings.Join(databases, ", ")), false)
	if err != nil || !ok {
		return err
	}
	replicas := map[string]string{}
	for _, service := range images.Services {
		out, err := kc.Cmd("get", "deployment", service, "-o", "jsonpath={.spec.replicas}").Output(ctx)
		count := strings.TrimSpace(out)
		if err != nil || count == "" {
			continue
		}
		if count == "0" {
			console.Warn("%s is scaled to 0, probably by an interrupted reset; it is restored to %d", service, DefaultReplicas)
			count = strconv.Itoa(DefaultReplicas)
		}
		replicas[service] = count
	}
	resetErr := resetK8sState(ctx, console, c, databases, replicas)
	restoreErr := restore(ctx, console, c, replicas)
	return errors.Join(resetErr, restoreErr)
}

func resetK8sState(ctx context.Context, console *ui.Console, c *cluster.Cluster, databases []string, replicas map[string]string) error {
	kc := c.Kube
	if len(replicas) > 0 {
		var names []string
		for service := range replicas {
			names = append(names, service)
		}
		if err := console.Run(ctx, "Stop the services", func(ctx context.Context, w io.Writer) error {
			args := append([]string{"scale", "--replicas=0"}, prefixed("deployment/", names)...)
			if err := kc.Cmd(args...).Run(ctx, w); err != nil {
				return err
			}
			selector := "app.kubernetes.io/name in (" + strings.Join(names, ",") + ")"
			return kc.Cmd("wait", "--for=delete", "pod", "--selector", selector, "--timeout="+ScaleTimeout.String()).Run(ctx, w)
		}); err != nil {
			return err
		}
	}
	vars := c.Vars()
	for _, service := range databases {
		if err := console.Run(ctx, "Clean and migrate "+service+" database", func(ctx context.Context, w io.Writer) error {
			return c.Migrate(ctx, w, vars, service, true)
		}); err != nil {
			return err
		}
	}
	if kc.Exists(ctx, "statefulset", "redpanda") {
		if err := console.Run(ctx, "Delete Kafka topics and consumer groups", func(ctx context.Context, w io.Writer) error {
			return clearKafka(ctx, w, func(args ...string) *execx.Cmd {
				return kc.Cmd(append([]string{"exec", "statefulset/redpanda", "--", "rpk"}, args...)...)
			})
		}); err != nil {
			return err
		}
	}
	if kc.Exists(ctx, "deployment", "mailpit") {
		if err := console.Run(ctx, "Delete Mailpit messages", func(ctx context.Context, w io.Writer) error {
			forward, err := kc.PortForward(ctx, "service/mailpit", MailpitUIPort)
			if err != nil {
				return err
			}
			defer forward.Close()
			return clearMailpit(ctx, forward.URL(), c.Config.Ops.Email, c.Config.Ops.Password)
		}); err != nil {
			return err
		}
	}
	return nil
}

func restore(ctx context.Context, console *ui.Console, c *cluster.Cluster, replicas map[string]string) error {
	if len(replicas) == 0 {
		return nil
	}
	kc := c.Kube
	ctx = context.WithoutCancel(ctx)
	if err := console.Run(ctx, "Start the services again", func(ctx context.Context, w io.Writer) error {
		for _, service := range images.Services {
			count, ok := replicas[service]
			if !ok {
				continue
			}
			if err := kc.Cmd("scale", "--replicas="+count, "deployment/"+service).Run(ctx, w); err != nil {
				return err
			}
		}
		for _, service := range images.Services {
			if _, ok := replicas[service]; !ok {
				continue
			}
			if err := kc.Rollout(ctx, w, "deployment/"+service, RolloutTimeout); err != nil {
				return err
			}
		}
		return nil
	}); err != nil {
		return err
	}
	return nil
}

func prefixed(prefix string, names []string) []string {
	out := make([]string, len(names))
	for i, name := range names {
		out[i] = prefix + name
	}
	return out
}
