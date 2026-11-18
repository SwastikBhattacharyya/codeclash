package kube

import (
	"context"
	_ "embed"
	"fmt"
	"io"
	"net"
	"slices"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/execx"
)

const (
	GatewayNodePort = 30080
	KindWait        = 3 * time.Minute
)

//go:embed kind-config.yaml
var kindConfig string

type KindState int

const (
	KindAbsent KindState = iota
	KindReady
	KindMismatch
	KindStopped
)

type Kind struct {
	Name       string
	PublicPort int
}

func (k Kind) Node() string {
	return k.Name + ControlSuffix
}

func (k Kind) Exists(ctx context.Context) bool {
	out, err := execx.Command("kind", "get", "clusters").Output(ctx)
	if err != nil {
		return false
	}
	return slices.Contains(strings.Fields(out), k.Name)
}

func (k Kind) bindings(ctx context.Context) ([]string, error) {
	out, err := execx.Command("docker", "inspect", "--format",
		`{{range $port, $binds := .HostConfig.PortBindings}}{{range $binds}}{{$port}} {{.HostIp}}:{{.HostPort}}{{"\n"}}{{end}}{{end}}`,
		k.Node()).Output(ctx)
	if err != nil {
		return nil, fmt.Errorf("inspecting the kind node %s: %w", k.Node(), err)
	}
	return strings.Split(strings.TrimSpace(out), "\n"), nil
}

func (k Kind) running(ctx context.Context) (bool, error) {
	out, err := execx.Command("docker", "inspect", "--format", "{{.State.Running}}", k.Node()).Output(ctx)
	if err != nil {
		return false, fmt.Errorf("inspecting the kind node %s: %w", k.Node(), err)
	}
	return strings.TrimSpace(out) == "true", nil
}

func (k Kind) State(ctx context.Context) (KindState, error) {
	if !k.Exists(ctx) {
		return KindAbsent, nil
	}
	bindings, err := k.bindings(ctx)
	if err != nil {
		return KindAbsent, err
	}
	want := fmt.Sprintf("%d/tcp 127.0.0.1:%d", GatewayNodePort, k.PublicPort)
	if !slices.Contains(bindings, want) {
		return KindMismatch, nil
	}
	running, err := k.running(ctx)
	if err != nil {
		return KindAbsent, err
	}
	if !running {
		return KindStopped, nil
	}
	return KindReady, nil
}

func (k Kind) Start(ctx context.Context, w io.Writer) error {
	return execx.Command("docker", "start", k.Node()).Run(ctx, w)
}

func (k Kind) HoldsPort(ctx context.Context, port int) bool {
	bindings, err := k.bindings(ctx)
	if err != nil {
		return false
	}
	suffix := " 127.0.0.1:" + strconv.Itoa(port)
	for _, binding := range bindings {
		if strings.HasSuffix(binding, suffix) {
			return true
		}
	}
	return false
}

func PortFree(port int) bool {
	listener, err := net.Listen("tcp", "127.0.0.1:"+strconv.Itoa(port))
	if err != nil {
		return false
	}
	listener.Close()
	return true
}

func (k Kind) Create(ctx context.Context, w io.Writer) error {
	config := strings.NewReplacer(
		"${CLUSTER_NAME}", k.Name,
		"${PUBLIC_PORT}", strconv.Itoa(k.PublicPort),
		"${GATEWAY_NODE_PORT}", strconv.Itoa(GatewayNodePort),
	).Replace(kindConfig)
	return execx.Command("kind", "create", "cluster", "--config", "/dev/stdin", "--wait", KindWait.String()).StdinString(config).Run(ctx, w)
}

func (k Kind) Delete(ctx context.Context, w io.Writer) error {
	return execx.Command("kind", "delete", "cluster", "--name", k.Name).Run(ctx, w)
}

func (k Kind) HasImage(ctx context.Context, image string) bool {
	_, err := execx.Command("docker", "exec", k.Node(), "crictl", "inspecti", "-q", image).Output(ctx)
	return err == nil
}
