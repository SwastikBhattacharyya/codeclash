package main

import (
	"context"
	"errors"
	"fmt"
	"os"
	"strings"

	"codeclash/internal/app"
	"codeclash/internal/config"
	"codeclash/internal/repo"
	"codeclash/internal/ui"
)

const usage = `Usage: ./codeclash [--profile NAME] [--yes] [COMMAND] [ARGS]

Without a command, opens the interactive menu.

Profiles (stored in $XDG_CONFIG_HOME/codeclash, mode 0600):
  configure              edit the active profile, or create the first one
  new                    create a profile
  profiles               list profiles
  use NAME               make NAME the active profile

Actions on the active profile:
  pipeline [STEPS...]    run steps in order; default: every step except reset, verify and contests
                           k8s:     reset build deploy admin questions verify contests
                           compose: up reset admin questions verify contests
  build                  build the images, tagged <version>-<git sha>[-dirty]   (k8s)
  deploy                 load the images and deploy to the cluster               (k8s)
  up | down              start or stop the docker compose infrastructure        (compose)
  reset                  wipe the databases, Kafka topics and groups, and mail
  seed-admin             create or reset the superadmin from the profile
  seed-questions         seed the question catalog in seed/ [--verify]
  seed-contests          seed the contests, contestants and submissions in seed/contests/
  status                 show the profile, images and pods or containers
  teardown               delete the kind cluster, or the namespace of an existing cluster (k8s)

Flags:
  --profile NAME         use NAME instead of the active profile for this run
  -y, --yes              answer yes to confirmations (external databases are still skipped)
  --verify               with seed-questions: submit every reference solution
`

type options struct {
	profile string
	yes     bool
	verify  bool
	help    bool
	args    []string
}

func parse(args []string) (options, error) {
	var parsed options
	for i := 0; i < len(args); i++ {
		arg := args[i]
		switch {
		case arg == "-h" || arg == "--help" || (arg == "help" && len(parsed.args) == 0):
			parsed.help = true
		case arg == "-y" || arg == "--yes":
			parsed.yes = true
		case arg == "--verify":
			parsed.verify = true
		case arg == "--profile":
			if i+1 >= len(args) {
				return parsed, errors.New("--profile needs a name")
			}
			i++
			parsed.profile = args[i]
		case strings.HasPrefix(arg, "--profile="):
			parsed.profile = strings.TrimPrefix(arg, "--profile=")
		case strings.HasPrefix(arg, "-"):
			return parsed, fmt.Errorf("unknown flag %s", arg)
		default:
			parsed.args = append(parsed.args, arg)
		}
	}
	return parsed, nil
}

func main() {
	os.Exit(run())
}

func run() int {
	parsed, err := parse(os.Args[1:])
	if err != nil {
		fmt.Fprint(os.Stderr, usage)
		fmt.Fprintln(os.Stderr, "\nerror:", err)
		return 2
	}
	if parsed.help {
		fmt.Print(usage)
		return 0
	}
	console := ui.NewConsole(parsed.yes)
	r, err := repo.Locate()
	if err != nil {
		console.Fail("%v", err)
		return 1
	}
	store, err := config.OpenStore()
	if err != nil {
		console.Fail("%v", err)
		return 1
	}
	a := &app.App{Repo: r, Store: store, Console: console}
	if err := load(a, parsed.profile); err != nil {
		if len(parsed.args) > 0 && !repairs[parsed.args[0]] {
			console.Fail("%v", err)
			return 1
		}
		console.Warn("%v", err)
	}
	if err := dispatch(a, parsed); err != nil {
		a.Report(err)
		return 1
	}
	return 0
}

var repairs = map[string]bool{"configure": true, "new": true, "profiles": true, "use": true}

func load(a *app.App, name string) error {
	if name == "" {
		active, err := a.Store.Active()
		if errors.Is(err, config.ErrNoProfile) {
			return nil
		}
		if err != nil {
			return err
		}
		name = active
	}
	profile, err := a.Store.Load(name)
	if err != nil {
		return err
	}
	a.Profile = profile
	return nil
}

func dispatch(a *app.App, parsed options) error {
	if len(parsed.args) == 0 {
		return a.Menu()
	}
	command, rest := parsed.args[0], parsed.args[1:]
	single := func(action func(ctx context.Context) error) error {
		if len(rest) > 0 {
			return fmt.Errorf("%s takes no arguments", command)
		}
		return a.Do(action)
	}
	switch command {
	case "configure":
		return single(a.Configure)
	case "new":
		return single(a.NewProfile)
	case "profiles":
		return listProfiles(a)
	case "use":
		if len(rest) != 1 {
			return errors.New("use needs exactly one profile name")
		}
		return a.Use(rest[0])
	case "pipeline":
		steps := rest
		if len(steps) == 0 {
			if a.Profile == nil {
				return errors.New("no profile configured yet: run ./codeclash configure")
			}
			steps = a.DefaultSteps()
		}
		return a.Do(func(ctx context.Context) error { return a.Pipeline(ctx, steps) })
	case "build":
		return single(a.Build)
	case "deploy":
		return single(a.Deploy)
	case "up":
		return single(a.Up)
	case "down":
		return single(a.Down)
	case "reset":
		return single(a.Reset)
	case "seed-admin":
		return single(a.SeedAdmin)
	case "seed-questions":
		return single(func(ctx context.Context) error { return a.SeedQuestions(ctx, parsed.verify) })
	case "seed-contests":
		return single(a.SeedContests)
	case "status":
		return single(a.Status)
	case "teardown":
		return single(a.Teardown)
	default:
		return fmt.Errorf("unknown command %s, see ./codeclash --help", command)
	}
}

func listProfiles(a *app.App) error {
	names, err := a.Store.List()
	if err != nil {
		return err
	}
	if len(names) == 0 {
		fmt.Println("No profiles yet. Run ./codeclash configure")
		return nil
	}
	active, _ := a.Store.Active()
	for _, name := range names {
		marker := "  "
		if name == active {
			marker = "* "
		}
		target := "?"
		if profile, err := a.Store.Load(name); err == nil {
			target = profile.Target
		}
		fmt.Printf("%s%s (%s)\n", marker, name, target)
	}
	return nil
}
