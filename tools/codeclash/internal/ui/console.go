package ui

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/charmbracelet/huh"
	"github.com/charmbracelet/lipgloss"
	"golang.org/x/term"
)

const (
	TailLines     = 40
	SpinnerPeriod = 100 * time.Millisecond
	LogDirMode    = 0o700
	LogFileMode   = 0o600
	BannerPadY    = 1
	BannerPadX    = 3
	BannerMarginY = 1
)

var (
	ErrAborted = errors.New("aborted")
	frames     = []string{"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"}
	accent     = lipgloss.Color("212")
	okColor    = lipgloss.Color("42")
	warnColor  = lipgloss.Color("214")
	errColor   = lipgloss.Color("196")
	muted      = lipgloss.Color("245")
)

type reported struct {
	err error
}

func (r *reported) Error() string {
	return r.err.Error()
}

func (r *reported) Unwrap() error {
	return r.err
}

func Reported(err error) bool {
	var r *reported
	return errors.As(err, &r)
}

type Console struct {
	out         io.Writer
	interactive bool
	yes         bool
	logPath     string
}

func NewConsole(yes bool) *Console {
	interactive := term.IsTerminal(int(os.Stdin.Fd())) && term.IsTerminal(int(os.Stdout.Fd()))
	return &Console{out: os.Stdout, interactive: interactive, yes: yes, logPath: errorLogPath()}
}

func errorLogPath() string {
	dir, err := os.UserCacheDir()
	if err != nil {
		dir = os.TempDir()
	}
	return filepath.Join(dir, "codeclash", "last-error.log")
}

func (c *Console) Interactive() bool {
	return c.interactive
}

func (c *Console) Yes() bool {
	return c.yes
}

func (c *Console) Banner(subtitle string) {
	title := lipgloss.NewStyle().Bold(true).Foreground(accent).Render("CodeClash")
	sub := lipgloss.NewStyle().Foreground(muted).Render(subtitle)
	box := lipgloss.NewStyle().Border(lipgloss.RoundedBorder()).BorderForeground(accent).Padding(BannerPadY, BannerPadX).Margin(BannerMarginY, 0).Align(lipgloss.Center)
	fmt.Fprintln(c.out, box.Render(lipgloss.JoinVertical(lipgloss.Center, title, sub)))
}

func (c *Console) Section(title string) {
	fmt.Fprintln(c.out)
	fmt.Fprintln(c.out, lipgloss.NewStyle().Bold(true).Foreground(accent).Render("▸ "+title))
}

func (c *Console) Info(format string, args ...any) {
	fmt.Fprintln(c.out, lipgloss.NewStyle().Foreground(muted).Render("  "+fmt.Sprintf(format, args...)))
}

func (c *Console) OK(format string, args ...any) {
	fmt.Fprintln(c.out, lipgloss.NewStyle().Foreground(okColor).Render("  ✓ "+fmt.Sprintf(format, args...)))
}

func (c *Console) Warn(format string, args ...any) {
	fmt.Fprintln(c.out, lipgloss.NewStyle().Foreground(warnColor).Render("  ! "+fmt.Sprintf(format, args...)))
}

func (c *Console) Fail(format string, args ...any) {
	fmt.Fprintln(os.Stderr, lipgloss.NewStyle().Foreground(errColor).Bold(true).Render("  ✗ "+fmt.Sprintf(format, args...)))
}

func (c *Console) Accent(s string) string {
	return lipgloss.NewStyle().Foreground(accent).Render(s)
}

func (c *Console) Errorf(format string, args ...any) error {
	err := fmt.Errorf(format, args...)
	c.Fail("%s", err.Error())
	return &reported{err: err}
}

type logBuffer struct {
	mu  sync.Mutex
	buf bytes.Buffer
}

func (l *logBuffer) Write(p []byte) (int, error) {
	l.mu.Lock()
	defer l.mu.Unlock()
	return l.buf.Write(p)
}

func (l *logBuffer) String() string {
	l.mu.Lock()
	defer l.mu.Unlock()
	return l.buf.String()
}

func (c *Console) Run(ctx context.Context, title string, step func(ctx context.Context, log io.Writer) error) error {
	log := &logBuffer{}
	stop := c.spin(title)
	err := func() error {
		defer stop()
		return step(ctx, log)
	}()
	if err == nil {
		c.OK("%s", title)
		return nil
	}
	if ctx.Err() != nil {
		c.Fail("%s (cancelled)", title)
		return &reported{err: ctx.Err()}
	}
	c.Fail("%s", title)
	output := log.String()
	if output != "" && !strings.HasSuffix(output, "\n") {
		output += "\n"
	}
	output += err.Error() + "\n"
	lines := strings.Split(strings.TrimRight(output, "\n"), "\n")
	if len(lines) > TailLines {
		lines = lines[len(lines)-TailLines:]
	}
	for _, line := range lines {
		fmt.Fprintln(os.Stderr, "    "+line)
	}
	if writeErr := c.saveLog(output); writeErr == nil {
		c.Info("Full output saved to %s", c.logPath)
	}
	return &reported{err: err}
}

func (c *Console) saveLog(output string) error {
	if err := os.MkdirAll(filepath.Dir(c.logPath), LogDirMode); err != nil {
		return err
	}
	return os.WriteFile(c.logPath, []byte(output), LogFileMode)
}

func (c *Console) spin(title string) func() {
	if !c.interactive {
		fmt.Fprintln(c.out, lipgloss.NewStyle().Foreground(muted).Render("  … "+title))
		return func() {}
	}
	done := make(chan struct{})
	finished := make(chan struct{})
	style := lipgloss.NewStyle().Foreground(accent)
	go func() {
		defer close(finished)
		ticker := time.NewTicker(SpinnerPeriod)
		defer ticker.Stop()
		started := time.Now()
		for frame := 0; ; frame++ {
			elapsed := time.Since(started).Truncate(time.Second)
			fmt.Fprintf(c.out, "\r\033[K  %s %s %s", style.Render(frames[frame%len(frames)]), title, lipgloss.NewStyle().Foreground(muted).Render(elapsed.String()))
			select {
			case <-done:
				fmt.Fprint(c.out, "\r\033[K")
				return
			case <-ticker.C:
			}
		}
	}()
	return func() {
		close(done)
		<-finished
	}
}

func (c *Console) Confirm(prompt string, fallback bool) (bool, error) {
	if c.yes {
		return true, nil
	}
	if !c.interactive {
		return false, errors.New(prompt + " needs confirmation: rerun with --yes or in a terminal")
	}
	value := fallback
	err := huh.NewConfirm().Title(prompt).Value(&value).Run()
	if err != nil {
		return false, normalize(err)
	}
	return value, nil
}

func (c *Console) ConfirmTyped(prompt string, expected string) (bool, error) {
	if !c.interactive {
		return false, nil
	}
	var value string
	err := huh.NewInput().Title(prompt).Description("Type " + expected + " to confirm, anything else skips").Value(&value).Run()
	if err != nil {
		return false, normalize(err)
	}
	return value == expected, nil
}

func (c *Console) Table(headers []string, rows [][]string) {
	widths := make([]int, len(headers))
	for i, header := range headers {
		widths[i] = lipgloss.Width(header)
	}
	for _, row := range rows {
		for i, cell := range row {
			widths[i] = max(widths[i], lipgloss.Width(cell))
		}
	}
	line := func(cells []string, style lipgloss.Style) string {
		parts := make([]string, len(cells))
		for i, cell := range cells {
			parts[i] = style.Render(cell + strings.Repeat(" ", widths[i]-lipgloss.Width(cell)))
		}
		return "  " + strings.Join(parts, "   ")
	}
	fmt.Fprintln(c.out, line(headers, lipgloss.NewStyle().Bold(true).Foreground(accent)))
	for _, row := range rows {
		fmt.Fprintln(c.out, line(row, lipgloss.NewStyle()))
	}
}

func normalize(err error) error {
	if errors.Is(err, huh.ErrUserAborted) {
		return ErrAborted
	}
	return err
}

func Normalize(err error) error {
	return normalize(err)
}
