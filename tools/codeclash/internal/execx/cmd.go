package execx

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"os"
	"os/exec"
	"strings"
	"syscall"
	"time"
)

const (
	KillGrace = 10 * time.Second
	WaitGrace = 5 * time.Second
)

type Cmd struct {
	name  string
	args  []string
	dir   string
	env   []string
	stdin io.Reader
}

type Error struct {
	Command string
	Stderr  string
	Err     error
}

func (e *Error) Error() string {
	detail := strings.TrimSpace(e.Stderr)
	if detail == "" {
		return fmt.Sprintf("%s: %v", e.Command, e.Err)
	}
	return fmt.Sprintf("%s: %v\n%s", e.Command, e.Err, detail)
}

func (e *Error) Unwrap() error {
	return e.Err
}

func Command(name string, args ...string) *Cmd {
	return &Cmd{name: name, args: args}
}

func (c *Cmd) Dir(dir string) *Cmd {
	c.dir = dir
	return c
}

func (c *Cmd) Env(env ...string) *Cmd {
	c.env = append(c.env, env...)
	return c
}

func (c *Cmd) Stdin(r io.Reader) *Cmd {
	c.stdin = r
	return c
}

func (c *Cmd) StdinString(s string) *Cmd {
	return c.Stdin(strings.NewReader(s))
}

func (c *Cmd) String() string {
	return strings.Join(append([]string{c.name}, c.args...), " ")
}

func (c *Cmd) build(ctx context.Context) *exec.Cmd {
	cmd := exec.CommandContext(ctx, c.name, c.args...)
	cmd.Dir = c.dir
	if len(c.env) > 0 {
		cmd.Env = append(os.Environ(), c.env...)
	}
	cmd.Stdin = c.stdin
	cmd.SysProcAttr = &syscall.SysProcAttr{Setpgid: true}
	cmd.Cancel = func() error {
		group := -cmd.Process.Pid
		time.AfterFunc(KillGrace, func() {
			_ = syscall.Kill(group, syscall.SIGKILL)
		})
		if err := syscall.Kill(group, syscall.SIGTERM); errors.Is(err, syscall.ESRCH) {
			return os.ErrProcessDone
		} else {
			return err
		}
	}
	cmd.WaitDelay = KillGrace + WaitGrace
	return cmd
}

func (c *Cmd) Run(ctx context.Context, w io.Writer) error {
	cmd := c.build(ctx)
	cmd.Stdout = w
	cmd.Stderr = w
	if err := cmd.Run(); err != nil {
		return &Error{Command: c.String(), Err: err}
	}
	return nil
}

func (c *Cmd) Pipe(ctx context.Context, stdout io.Writer, stderr io.Writer) error {
	cmd := c.build(ctx)
	cmd.Stdout = stdout
	cmd.Stderr = stderr
	if err := cmd.Run(); err != nil {
		return &Error{Command: c.String(), Err: err}
	}
	return nil
}

func (c *Cmd) Output(ctx context.Context) (string, error) {
	cmd := c.build(ctx)
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	if err := cmd.Run(); err != nil {
		return stdout.String(), &Error{Command: c.String(), Stderr: stderr.String(), Err: err}
	}
	return stdout.String(), nil
}

func (c *Cmd) Interactive(ctx context.Context) error {
	cmd := exec.CommandContext(ctx, c.name, c.args...)
	cmd.Dir = c.dir
	if len(c.env) > 0 {
		cmd.Env = append(os.Environ(), c.env...)
	}
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		return &Error{Command: c.String(), Err: err}
	}
	return nil
}

func Chain(ctx context.Context, w io.Writer, from *Cmd, to *Cmd) error {
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()
	reader, writer := io.Pipe()
	done := make(chan error, 1)
	go func() {
		err := from.Pipe(ctx, writer, w)
		writer.CloseWithError(err)
		done <- err
	}()
	toErr := to.Stdin(reader).Run(ctx, w)
	if toErr != nil {
		reader.CloseWithError(toErr)
		cancel()
	} else {
		_, _ = io.Copy(io.Discard, reader)
	}
	fromErr := <-done
	if fromErr != nil && toErr == nil {
		return fromErr
	}
	return toErr
}

func Exists(name string) bool {
	_, err := exec.LookPath(name)
	return err == nil
}
