package kube

import (
	"bufio"
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"regexp"
	"strings"
	"time"

	"codeclash/internal/execx"
)

const (
	PartOfLabel   = "app.kubernetes.io/part-of"
	PartOfValue   = "codeclash"
	NameLabel     = "app.kubernetes.io/name"
	ModeLabel     = "codeclash.dev/mode"
	ReachTimeout  = 20 * time.Second
	JobTimeout    = 10 * time.Minute
	JobPoll       = 3 * time.Second
	ForwardWait   = 30 * time.Second
	KindProvider  = "kind://"
	K3sMarker     = "+k3s"
	ControlSuffix = "-control-plane"
)

type Distro string

const (
	DistroKind  Distro = "kind"
	DistroK3s   Distro = "k3s"
	DistroOther Distro = "other"
)

var forwardLine = regexp.MustCompile(`Forwarding from 127\.0\.0\.1:(\d+)`)

type Kube struct {
	Context   string
	Namespace string
}

func (k Kube) Cmd(args ...string) *execx.Cmd {
	return execx.Command("kubectl", append([]string{"--context", k.Context, "--namespace", k.Namespace}, args...)...)
}

func (k Kube) ClusterCmd(args ...string) *execx.Cmd {
	return execx.Command("kubectl", append([]string{"--context", k.Context}, args...)...)
}

func Contexts(ctx context.Context) ([]string, error) {
	out, err := execx.Command("kubectl", "config", "get-contexts", "-o", "name").Output(ctx)
	if err != nil {
		return nil, err
	}
	return strings.Fields(out), nil
}

func (k Kube) Reachable(ctx context.Context) error {
	ctx, cancel := context.WithTimeout(ctx, ReachTimeout)
	defer cancel()
	if _, err := k.ClusterCmd("get", "--raw", "/readyz").Output(ctx); err != nil {
		return fmt.Errorf("cluster behind context %s is not reachable: %w", k.Context, err)
	}
	return nil
}

type Nodes struct {
	Distro   Distro
	KindName string
	First    string
	Count    int
}

func (k Kube) Nodes(ctx context.Context) (Nodes, error) {
	out, err := k.ClusterCmd("get", "nodes", "-o", "json").Output(ctx)
	if err != nil {
		return Nodes{}, err
	}
	var nodes struct {
		Items []struct {
			Metadata struct {
				Name string `json:"name"`
			} `json:"metadata"`
			Spec struct {
				ProviderID string `json:"providerID"`
			} `json:"spec"`
			Status struct {
				NodeInfo struct {
					KubeletVersion string `json:"kubeletVersion"`
				} `json:"nodeInfo"`
			} `json:"status"`
		} `json:"items"`
	}
	if err := json.Unmarshal([]byte(out), &nodes); err != nil {
		return Nodes{}, err
	}
	if len(nodes.Items) == 0 {
		return Nodes{}, errors.New("the cluster has no nodes")
	}
	node := nodes.Items[0]
	result := Nodes{Distro: DistroOther, First: node.Metadata.Name, Count: len(nodes.Items)}
	switch {
	case strings.HasPrefix(node.Spec.ProviderID, KindProvider):
		result.Distro = DistroKind
		result.KindName = strings.TrimSuffix(node.Metadata.Name, ControlSuffix)
	case strings.Contains(node.Status.NodeInfo.KubeletVersion, K3sMarker):
		result.Distro = DistroK3s
	}
	return result, nil
}

func (k Kube) NodeAddress(ctx context.Context) string {
	out, err := k.ClusterCmd("get", "nodes", "-o", `jsonpath={.items[0].status.addresses[?(@.type=="InternalIP")].address}`).Output(ctx)
	if err != nil || strings.TrimSpace(out) == "" {
		return "<node-ip>"
	}
	return strings.Fields(out)[0]
}

func (k Kube) Apply(ctx context.Context, w io.Writer, manifest string) error {
	return k.Cmd("apply", "--server-side", "--force-conflicts", "-f", "-").StdinString(manifest).Run(ctx, w)
}

func (k Kube) ApplyCluster(ctx context.Context, w io.Writer, manifest string) error {
	return k.ClusterCmd("apply", "--server-side", "--force-conflicts", "-f", "-").StdinString(manifest).Run(ctx, w)
}

func (k Kube) ApplyObject(ctx context.Context, w io.Writer, object any) error {
	data, err := json.Marshal(object)
	if err != nil {
		return err
	}
	return k.Apply(ctx, w, string(data))
}

func (k Kube) Exists(ctx context.Context, resource string, name string) bool {
	found, err := k.Lookup(ctx, resource, name)
	return err == nil && found
}

func (k Kube) Lookup(ctx context.Context, resource string, name string) (bool, error) {
	out, err := k.Cmd("get", resource, name, "--ignore-not-found", "-o", "name").Output(ctx)
	if err != nil {
		return false, err
	}
	return strings.TrimSpace(out) != "", nil
}

func (k Kube) NamespaceExists(ctx context.Context) bool {
	_, err := k.ClusterCmd("get", "namespace", k.Namespace, "-o", "name").Output(ctx)
	return err == nil
}

func (k Kube) Delete(ctx context.Context, w io.Writer, args ...string) error {
	return k.Cmd(append([]string{"delete", "--ignore-not-found"}, args...)...).Run(ctx, w)
}

func (k Kube) Rollout(ctx context.Context, w io.Writer, resource string, timeout time.Duration) error {
	return k.Cmd("rollout", "status", resource, "--timeout="+timeout.String()).Run(ctx, w)
}

func (k Kube) Secret(ctx context.Context, name string) (map[string]string, map[string]string, error) {
	out, err := k.Cmd("get", "secret", name, "-o", "json").Output(ctx)
	if err != nil {
		return nil, nil, err
	}
	var secret struct {
		Metadata struct {
			Labels map[string]string `json:"labels"`
		} `json:"metadata"`
		Data map[string]string `json:"data"`
	}
	if err := json.Unmarshal([]byte(out), &secret); err != nil {
		return nil, nil, err
	}
	values := map[string]string{}
	for key, encoded := range secret.Data {
		decoded, err := base64.StdEncoding.DecodeString(encoded)
		if err != nil {
			return nil, nil, err
		}
		values[key] = string(decoded)
	}
	return values, secret.Metadata.Labels, nil
}

func (k Kube) ApplySecret(ctx context.Context, w io.Writer, name string, mode string, values map[string]string) error {
	data := map[string]string{}
	for key, value := range values {
		data[key] = base64.StdEncoding.EncodeToString([]byte(value))
	}
	return k.ApplyObject(ctx, w, map[string]any{
		"apiVersion": "v1",
		"kind":       "Secret",
		"type":       "Opaque",
		"metadata": map[string]any{
			"name":      name,
			"namespace": k.Namespace,
			"labels":    map[string]string{ModeLabel: mode, PartOfLabel: PartOfValue},
		},
		"data": data,
	})
}

func (k Kube) ApplyConfigMap(ctx context.Context, w io.Writer, name string, data map[string]string) error {
	return k.ApplyObject(ctx, w, map[string]any{
		"apiVersion": "v1",
		"kind":       "ConfigMap",
		"metadata": map[string]any{
			"name":      name,
			"namespace": k.Namespace,
			"labels":    map[string]string{PartOfLabel: PartOfValue},
		},
		"data": data,
	})
}

func (k Kube) WaitJob(ctx context.Context, w io.Writer, job string) error {
	deadline := time.Now().Add(JobTimeout)
	for time.Now().Before(deadline) {
		out, _ := k.Cmd("get", "job", job, "-o", `jsonpath={range .status.conditions[?(@.status=="True")]}{.type}{" "}{end}`).Output(ctx)
		conditions := " " + out + " "
		switch {
		case strings.Contains(conditions, " Complete "):
			_ = k.Cmd("logs", "job/"+job, "--tail=20").Run(ctx, w)
			return nil
		case strings.Contains(conditions, " Failed "):
			_ = k.Cmd("logs", "job/"+job, "--tail=60").Run(ctx, w)
			return fmt.Errorf("job %s failed", job)
		}
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-time.After(JobPoll):
		}
	}
	_ = k.Cmd("logs", "job/"+job, "--tail=60").Run(ctx, w)
	return fmt.Errorf("timed out waiting for job %s", job)
}

type Forward struct {
	Port   string
	cancel context.CancelFunc
	done   chan struct{}
}

func (f *Forward) URL() string {
	return "http://127.0.0.1:" + f.Port
}

func (f *Forward) Close() {
	f.cancel()
	<-f.done
}

func (k Kube) PortForward(ctx context.Context, target string, remotePort int) (*Forward, error) {
	ctx, cancel := context.WithCancel(ctx)
	reader, writer := io.Pipe()
	done := make(chan struct{})
	result := make(chan error, 1)
	go func() {
		defer close(done)
		err := k.Cmd("port-forward", "--address", "127.0.0.1", target, fmt.Sprintf(":%d", remotePort)).Run(ctx, writer)
		writer.CloseWithError(err)
		result <- err
	}()
	ports := make(chan string, 1)
	var output strings.Builder
	go func() {
		scanner := bufio.NewScanner(reader)
		sent := false
		for scanner.Scan() {
			line := scanner.Text()
			if !sent {
				output.WriteString(line + "\n")
				if match := forwardLine.FindStringSubmatch(line); match != nil {
					ports <- match[1]
					sent = true
				}
			}
		}
		if !sent {
			close(ports)
		}
	}()
	select {
	case port, ok := <-ports:
		if ok {
			return &Forward{Port: port, cancel: cancel, done: done}, nil
		}
		cancel()
		<-done
		return nil, fmt.Errorf("port-forward to %s failed: %v %s", target, <-result, output.String())
	case <-time.After(ForwardWait):
		cancel()
		<-done
		return nil, fmt.Errorf("port-forward to %s did not start", target)
	}
}
