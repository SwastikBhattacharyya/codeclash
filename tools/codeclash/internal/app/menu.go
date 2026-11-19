package app

import (
	"context"
	"errors"
	"fmt"
	"os"
	"os/signal"
	"slices"
	"strings"
	"syscall"

	"github.com/charmbracelet/huh"

	"codeclash/internal/ui"
)

const (
	StepUp        = "up"
	StepReset     = "reset"
	StepBuild     = "build"
	StepDeploy    = "deploy"
	StepAdmin     = "admin"
	StepQuestions = "questions"
	StepVerify    = "verify"
	StepContests  = "contests"
)

type step struct {
	name        string
	label       string
	defaultOn   bool
	destructive bool
}

func (a *App) steps() []step {
	if a.Profile != nil && a.Profile.IsK8s() {
		return []step{
			{StepReset, "Reset databases, Kafka and mail", false, true},
			{StepBuild, "Build images", true, false},
			{StepDeploy, "Deploy to the cluster", true, false},
			{StepAdmin, "Seed the superadmin", true, false},
			{StepQuestions, "Seed questions", true, false},
			{StepVerify, "Verify reference solutions (with questions)", false, false},
			{StepContests, "Seed contests, contestants and submissions", false, false},
		}
	}
	return []step{
		{StepUp, "Start the infrastructure", true, false},
		{StepReset, "Reset databases, Kafka and mail", false, true},
		{StepAdmin, "Seed the superadmin", true, false},
		{StepQuestions, "Seed questions", true, false},
		{StepVerify, "Verify reference solutions (with questions)", false, false},
		{StepContests, "Seed contests, contestants and submissions", false, false},
	}
}

func (a *App) StepNames() []string {
	var names []string
	for _, s := range a.steps() {
		names = append(names, s.name)
	}
	return names
}

func (a *App) DefaultSteps() []string {
	var names []string
	for _, s := range a.steps() {
		if s.defaultOn {
			names = append(names, s.name)
		}
	}
	return names
}

func (a *App) ChooseSteps() ([]string, error) {
	if err := a.interactiveOnly("choosing pipeline steps"); err != nil {
		return nil, err
	}
	selected := a.DefaultSteps()
	var options []huh.Option[string]
	for _, s := range a.steps() {
		options = append(options, huh.NewOption(s.label, s.name))
	}
	err := run(huh.NewMultiSelect[string]().Title("Pipeline steps, run in this order").Options(options...).Value(&selected))
	return selected, err
}

func (a *App) Pipeline(ctx context.Context, selected []string) error {
	if err := a.ready(); err != nil {
		return err
	}
	known := a.StepNames()
	for _, name := range selected {
		if !slices.Contains(known, name) {
			return a.Console.Errorf("unknown step %s for a %s profile, choose from %s", name, a.Profile.Target, strings.Join(known, ", "))
		}
	}
	if len(selected) == 0 {
		a.Console.Info("No steps selected")
		return nil
	}
	if slices.Contains(selected, StepVerify) && !slices.Contains(selected, StepQuestions) {
		selected = append(selected, StepQuestions)
	}
	var ordered []string
	for _, name := range known {
		if slices.Contains(selected, name) && name != StepVerify {
			ordered = append(ordered, name)
		}
	}
	a.Console.Section("Pipeline for " + a.Profile.Name)
	a.Console.Table([]string{"Setting", "Value"}, a.Summary())
	a.Console.Info("Steps: %s", strings.Join(ordered, " → "))
	ok, err := a.Console.Confirm("Run these steps?", true)
	if err != nil || !ok {
		return err
	}
	verify := slices.Contains(selected, StepVerify)
	for _, name := range ordered {
		var err error
		switch name {
		case StepUp:
			err = a.Up(ctx)
		case StepReset:
			err = a.Reset(ctx)
		case StepBuild:
			err = a.Build(ctx)
		case StepDeploy:
			err = a.Deploy(ctx)
		case StepAdmin:
			err = a.SeedAdmin(ctx)
		case StepQuestions:
			err = a.SeedQuestions(ctx, verify)
		case StepContests:
			err = a.SeedContests(ctx)
		}
		if err != nil {
			return err
		}
	}
	a.Console.Section("Pipeline finished")
	return nil
}

type entry struct {
	label  string
	action func(ctx context.Context) error
}

func (a *App) entries() []entry {
	profileEntries := []entry{
		{"Switch or create profile", a.SwitchProfile},
		{"Configure this profile", a.Configure},
	}
	if a.Profile == nil {
		return append(profileEntries[:1], entry{"Quit", nil})
	}
	pipeline := entry{"Run pipeline…", func(ctx context.Context) error {
		selected, err := a.ChooseSteps()
		if err != nil {
			return err
		}
		return a.Pipeline(ctx, selected)
	}}
	questions := entry{"Seed questions", func(ctx context.Context) error { return a.SeedQuestions(ctx, false) }}
	verify := entry{"Seed questions and verify solutions", func(ctx context.Context) error { return a.SeedQuestions(ctx, true) }}
	contests := entry{"Seed contests", a.SeedContests}
	var entries []entry
	if a.Profile.IsK8s() {
		entries = []entry{pipeline,
			{"Build images", a.Build},
			{"Deploy", a.Deploy},
			{"Reset databases, Kafka and mail", a.Reset},
			{"Seed superadmin", a.SeedAdmin},
			questions, verify, contests,
			{"Status", a.Status},
			{"Teardown", a.Teardown},
		}
	} else {
		entries = []entry{pipeline,
			{"Start infrastructure", a.Up},
			{"Stop infrastructure", a.Down},
			{"Reset databases, Kafka and mail", a.Reset},
			{"Seed superadmin", a.SeedAdmin},
			questions, verify, contests,
			{"Status", a.Status},
		}
	}
	entries = append(entries, profileEntries...)
	return append(entries, entry{"Quit", nil})
}

func (a *App) Menu() error {
	if err := a.interactiveOnly("the menu"); err != nil {
		return err
	}
	a.Console.Banner("Build, deploy, reset and seed")
	if a.Profile == nil {
		a.Console.Info("No profile yet, let's create one")
		if err := a.Do(a.NewProfile); err != nil && !errors.Is(err, ui.ErrAborted) {
			return err
		}
	}
	for {
		entries := a.entries()
		labels := make([]huh.Option[int], len(entries))
		for i, e := range entries {
			labels[i] = huh.NewOption(e.label, i)
		}
		title := "No profile"
		if a.Profile != nil {
			title = fmt.Sprintf("Profile %s (%s)", a.Profile.Name, a.Profile.Target)
		}
		choice := 0
		if err := run(huh.NewSelect[int]().Title(title).Options(labels...).Value(&choice)); err != nil {
			if errors.Is(err, ui.ErrAborted) {
				return nil
			}
			return err
		}
		chosen := entries[choice]
		if chosen.action == nil {
			return nil
		}
		if err := a.Do(chosen.action); err != nil {
			a.report(err)
		}
	}
}

func (a *App) Do(action func(ctx context.Context) error) error {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	go func() {
		<-ctx.Done()
		stop()
	}()
	return action(ctx)
}

func (a *App) report(err error) {
	switch {
	case errors.Is(err, ui.ErrAborted):
		a.Console.Info("Cancelled")
	case ui.Reported(err):
	default:
		a.Console.Fail("%v", err)
	}
}

func (a *App) Report(err error) {
	a.report(err)
}
