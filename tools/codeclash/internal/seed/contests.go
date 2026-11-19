package seed

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"slices"
	"sort"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/images"
	"codeclash/internal/pg"
	"codeclash/internal/repo"
	"codeclash/internal/ui"
)

const (
	OpenLead            = 35 * time.Second
	LiveWindow          = time.Hour
	SyncWait            = 3 * time.Minute
	SyncPoll            = 2 * time.Second
	JudgeWait           = 15 * time.Minute
	JudgePoll           = 2 * time.Second
	RegistrationLead    = 24 * time.Hour
	RegistrationSpacing = 7 * time.Minute
	CreationLead        = 7 * 24 * time.Hour
	ContestantDomain    = "example.test"
	MinPoints           = 1
	MaxPoints           = 10000
	ShiftedMarker       = "ok"
	contestantColumns   = 2
	leaderboardColumns  = 4

	VariantCorrect = "correct"
	VariantNaive   = "naive"
	VariantBuggy   = "buggy"
	VariantCE      = "ce"
	VariantRE      = "re"
	commonVariants = "common"
)

var variantStates = map[string]string{
	VariantCorrect: "correct",
	VariantNaive:   "time_limit_exceeded",
	VariantBuggy:   "incorrect",
	VariantCE:      "compilation_error",
	VariantRE:      "runtime_error",
}

var contestPermissions = []string{
	"problem:category:write", "problem:tag:write", "problem:question:write", "problem:test_case:write", "contest:contest:write",
}

const contestantQuery = `select id, coalesce(verification_token::text, '') from users where username = :'username';
`

const syncedSubmissionsQuery = `select count(*)
from json_to_recordset(:'expected'::json) as x(id uuid, state text)
join submissions s on s.id = x.id and s.state = x.state;
`

const problemShift = `begin;
update contests set live_at = :'live_at', end_at = :'end_at', opened_at = :'live_at', created_at = :'created_at', updated_at = now()
where id = :'contest_id';
update submissions s set created_at = x.created_at, updated_at = now()
from json_to_recordset(:'submissions'::json) as x(id uuid, created_at timestamptz)
where s.id = x.id;
select case when moved.contests = 1 and moved.submissions = :'submission_count'::int then 'ok'
  else ('problem shift moved ' || moved.contests || ' contests and ' || moved.submissions || ' of ' || :'submission_count' || ' submissions')::int::text end
from (select
  (select count(*) from contests where id = :'contest_id' and live_at = :'live_at') as contests,
  (select count(*) from submissions s
   join json_to_recordset(:'submissions'::json) as x(id uuid, created_at timestamptz) on s.id = x.id and s.created_at = x.created_at) as submissions
) moved;
commit;
`

const contestShift = `begin;
update contests set live_at = :'live_at', end_at = :'end_at', opened_at = :'live_at', created_at = :'created_at'
where id = :'contest_id';
update registrations r set registered_at = x.registered_at
from json_to_recordset(:'registrations'::json) as x(user_id uuid, registered_at timestamptz)
where r.contest_id = :'contest_id' and r.user_id = x.user_id;
update submissions s set created_at = x.created_at, updated_at = now()
from json_to_recordset(:'submissions'::json) as x(id uuid, created_at timestamptz)
where s.id = x.id;
delete from leaderboard_entries where contest_id = :'contest_id';
with solves as (
  select r.user_id, cq.points, min(s.created_at) as solved_at
  from contests c
  join registrations r on r.contest_id = c.id
  join contest_questions cq on cq.contest_id = c.id
  join submissions s on s.user_id = r.user_id and s.question_id = cq.question_id
  where c.id = :'contest_id'
    and s.state = 'correct'
    and s.created_at >= greatest(c.live_at, r.registered_at)
    and s.created_at < c.end_at
  group by r.user_id, cq.question_id, cq.points
),
totals as (
  select r.user_id,
    coalesce(sum(solves.points), 0)::int as points,
    count(solves.user_id)::int as solved,
    max(solves.solved_at) as last_solve_at
  from registrations r
  left join solves on solves.user_id = r.user_id
  where r.contest_id = :'contest_id'
  group by r.user_id
)
insert into leaderboard_entries(contest_id, user_id, points, solved, last_solve_at, rank)
select :'contest_id', user_id, points, solved, last_solve_at,
  rank() over (order by points desc, last_solve_at asc nulls last)
from totals;
select case when moved.contests = 1 and moved.registrations = :'registration_count'::int
    and moved.submissions = :'submission_count'::int then 'ok'
  else ('contest shift moved ' || moved.contests || ' contests, ' || moved.registrations || ' of ' || :'registration_count'
    || ' registrations and ' || moved.submissions || ' of ' || :'submission_count' || ' submissions')::int::text end
from (select
  (select count(*) from contests where id = :'contest_id' and live_at = :'live_at') as contests,
  (select count(*) from registrations where contest_id = :'contest_id' and registered_at < :'live_at') as registrations,
  (select count(*) from submissions s
   join json_to_recordset(:'submissions'::json) as x(id uuid, created_at timestamptz) on s.id = x.id and s.created_at = x.created_at) as submissions
) moved;
commit;
`

const leaderboardQuery = `select l.rank, u.username, l.points, l.solved
from leaderboard_entries l join users u on u.id = l.user_id
where l.contest_id = :'contest_id'
order by l.rank, u.username;
`

type Contestant struct {
	Username string `json:"username"`
	Name     string `json:"name"`
}

type ContestQuestion struct {
	Slug   string `json:"slug"`
	Points int    `json:"points"`
}

type ContestMeta struct {
	Name               string            `json:"name"`
	Description        string            `json:"description"`
	StartOffsetMinutes int               `json:"startOffsetMinutes"`
	DurationMinutes    int               `json:"durationMinutes"`
	Questions          []ContestQuestion `json:"questions"`
	Registrations      []string          `json:"registrations"`
}

type ScriptEntry struct {
	User     string `json:"user"`
	Question string `json:"question"`
	Variant  string `json:"variant"`
	Minute   int    `json:"minute"`
}

type Contest struct {
	Slug   string
	Meta   ContestMeta
	Script []ScriptEntry
}

func (c *Contest) Upcoming() bool {
	return c.Meta.StartOffsetMinutes > 0
}

func (c *Contest) ElapsedMinutes() int {
	if c.Upcoming() {
		return 0
	}
	return min(-c.Meta.StartOffsetMinutes, c.Meta.DurationMinutes)
}

type ContestCatalog struct {
	Catalog  *Catalog
	Contests []*Contest
	Users    []Contestant
	code     map[string]string
}

func readJSON(path string, target any) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return err
	}
	if err := json.Unmarshal(data, target); err != nil {
		return fmt.Errorf("%s: %w", path, err)
	}
	return nil
}

func LoadContests(r *repo.Repo) (*ContestCatalog, error) {
	catalog, err := loadNamedCatalog(r.Path("seed"))
	if err != nil {
		return nil, err
	}
	contestsDir := filepath.Join(catalog.Dir, "contests")
	contests := &ContestCatalog{Catalog: catalog, code: map[string]string{}}
	if err := readJSON(filepath.Join(contestsDir, "users.json"), &contests.Users); err != nil {
		return nil, err
	}
	entries, err := os.ReadDir(contestsDir)
	if err != nil {
		return nil, err
	}
	for _, entry := range entries {
		if !entry.IsDir() {
			continue
		}
		contest := &Contest{Slug: entry.Name()}
		dir := filepath.Join(contestsDir, entry.Name())
		if err := readJSON(filepath.Join(dir, "contest.json"), &contest.Meta); err != nil {
			return nil, err
		}
		if err := readJSON(filepath.Join(dir, "script.json"), &contest.Script); err != nil {
			return nil, err
		}
		questions, err := loadQuestions(catalog.Dir, filepath.Join(dir, "questions"))
		if err != nil {
			return nil, err
		}
		catalog.Questions = append(catalog.Questions, questions...)
		contests.Contests = append(contests.Contests, contest)
	}
	sort.SliceStable(contests.Contests, func(i, j int) bool {
		return contests.Contests[i].Meta.StartOffsetMinutes < contests.Contests[j].Meta.StartOffsetMinutes
	})
	if err := contests.validate(); err != nil {
		return nil, err
	}
	return contests, nil
}

func (c *ContestCatalog) question(slug string) *Question {
	for _, question := range c.Catalog.Questions {
		if question.Slug == slug {
			return question
		}
	}
	return nil
}

func (c *ContestCatalog) variantPath(slug string, variant string) string {
	switch variant {
	case VariantCorrect:
		return filepath.Join(c.Catalog.Dir, "solutions", slug+".cpp")
	case VariantCE, VariantRE:
		return filepath.Join(c.Catalog.Dir, "variants", commonVariants, variant+".cpp")
	default:
		return filepath.Join(c.Catalog.Dir, "variants", slug, variant+".cpp")
	}
}

func (c *ContestCatalog) validate() error {
	users := map[string]bool{}
	for _, user := range c.Users {
		if users[user.Username] {
			return fmt.Errorf("users.json lists %s twice", user.Username)
		}
		users[user.Username] = true
	}
	slugs := map[string]bool{}
	for _, question := range c.Catalog.Questions {
		if slugs[question.Slug] {
			return fmt.Errorf("question %s appears in more than one contest", question.Slug)
		}
		slugs[question.Slug] = true
	}
	names := map[string]bool{}
	for _, contest := range c.Contests {
		where := "contests/" + contest.Slug
		name := strings.ToLower(contest.Meta.Name)
		if names[name] {
			return fmt.Errorf("%s: contest name %s is used twice", where, contest.Meta.Name)
		}
		names[name] = true
		if contest.Meta.DurationMinutes <= 0 {
			return fmt.Errorf("%s: durationMinutes must be positive", where)
		}
		if len(contest.Meta.Questions) == 0 {
			return fmt.Errorf("%s: no questions", where)
		}
		points := map[string]bool{}
		for _, question := range contest.Meta.Questions {
			if points[question.Slug] {
				return fmt.Errorf("%s: %s is listed twice", where, question.Slug)
			}
			if c.question(question.Slug) == nil {
				return fmt.Errorf("%s: unknown question %s", where, question.Slug)
			}
			if question.Points < MinPoints || question.Points > MaxPoints {
				return fmt.Errorf("%s: %s is worth %d points, outside %d-%d", where, question.Slug, question.Points, MinPoints, MaxPoints)
			}
			points[question.Slug] = true
		}
		registered := map[string]bool{}
		for _, username := range contest.Meta.Registrations {
			if !users[username] {
				return fmt.Errorf("%s: registration for unknown user %s", where, username)
			}
			if registered[username] {
				return fmt.Errorf("%s: %s is registered twice", where, username)
			}
			registered[username] = true
		}
		if contest.Upcoming() && len(contest.Script) > 0 {
			return fmt.Errorf("%s: an upcoming contest can't have submissions", where)
		}
		for index, entry := range contest.Script {
			at := fmt.Sprintf("%s/script.json entry %d", where, index+1)
			if !registered[entry.User] {
				return fmt.Errorf("%s: %s is not registered", at, entry.User)
			}
			if !points[entry.Question] {
				return fmt.Errorf("%s: %s is not in the contest", at, entry.Question)
			}
			if _, ok := variantStates[entry.Variant]; !ok {
				return fmt.Errorf("%s: unknown variant %s", at, entry.Variant)
			}
			if entry.Minute < 0 || entry.Minute >= contest.ElapsedMinutes() {
				return fmt.Errorf("%s: minute %d is outside the %d minutes that have passed", at, entry.Minute, contest.ElapsedMinutes())
			}
			path := c.variantPath(entry.Question, entry.Variant)
			if _, ok := c.code[path]; ok {
				continue
			}
			data, err := os.ReadFile(path)
			if err != nil {
				return fmt.Errorf("%s: %w", at, err)
			}
			c.code[path] = string(data)
		}
	}
	return nil
}

type ContestOptions struct {
	Username           string
	Password           string
	ContestantPassword string
}

type contestant struct {
	id  string
	api *API
}

type contestRun struct {
	console    *ui.Console
	catalog    *ContestCatalog
	gateway    string
	postgres   pg.Postgres
	admin      *API
	options    ContestOptions
	questions  map[string]string
	categories map[string]string
	tags       map[string]string
	users      map[string]*contestant
}

func Contests(ctx context.Context, console *ui.Console, r *repo.Repo, gateway string, postgres pg.Postgres, options ContestOptions) error {
	console.Section("Contests")
	if options.ContestantPassword == "" {
		return console.Errorf("the profile has no contestant password, run configure")
	}
	catalog, err := LoadContests(r)
	if err != nil {
		return console.Errorf("reading seed/contests: %v", err)
	}
	run := &contestRun{
		console: console, catalog: catalog, gateway: gateway, postgres: postgres, admin: NewAPI(gateway), options: options,
		questions: map[string]string{}, users: map[string]*contestant{},
	}
	if err := run.login(ctx); err != nil {
		return err
	}
	var missing []*Contest
	for _, contest := range catalog.Contests {
		existing, err := run.admin.FindContest(ctx, contest.Meta.Name)
		if err != nil {
			return console.Errorf("%v", err)
		}
		if existing != "" {
			console.Info("%s already exists, skipped", contest.Meta.Name)
			continue
		}
		missing = append(missing, contest)
	}
	if len(missing) == 0 {
		console.OK("Every contest is already seeded")
		return nil
	}
	if !images.Present(ctx, images.SandboxImage) {
		if err := console.Run(ctx, "Build "+images.SandboxImage, func(ctx context.Context, w io.Writer) error {
			return images.BuildSandbox(ctx, w, r)
		}); err != nil {
			return err
		}
	}
	if err := console.Run(ctx, fmt.Sprintf("Generate tests for %d contest questions in %s", len(catalog.Catalog.Questions), images.SandboxImage), catalog.Catalog.Generate); err != nil {
		return err
	}
	if err := run.seedQuestions(ctx); err != nil {
		return err
	}
	if err := run.seedContestants(ctx); err != nil {
		return err
	}
	for _, contest := range missing {
		if err := run.seedContest(ctx, contest); err != nil {
			return err
		}
	}
	return nil
}

func (s *contestRun) login(ctx context.Context) error {
	return s.console.Run(ctx, "Log in as "+s.options.Username, func(ctx context.Context, w io.Writer) error {
		permissions, err := s.admin.Login(ctx, s.options.Username, s.options.Password)
		if err != nil {
			return err
		}
		for _, permission := range contestPermissions {
			if !slices.Contains(permissions, permission) {
				return fmt.Errorf("%s lacks %s, give the account the admin or superadmin role", s.options.Username, permission)
			}
		}
		return nil
	})
}

func (s *contestRun) seedQuestions(ctx context.Context) error {
	if err := s.console.Run(ctx, "Ensure categories and tags", func(ctx context.Context, w io.Writer) error {
		var err error
		if s.categories, err = ensureNamed(ctx, w, s.admin, "categories", s.catalog.Catalog.Categories); err != nil {
			return err
		}
		s.tags, err = ensureNamed(ctx, w, s.admin, "tags", s.catalog.Catalog.Tags)
		return err
	}); err != nil {
		return err
	}
	for _, question := range s.catalog.Catalog.Questions {
		var outcome string
		if err := s.console.Run(ctx, question.Meta.Title+" (hidden)", func(ctx context.Context, w io.Writer) error {
			id, result, err := seedQuestion(ctx, s.admin, question, s.categories, s.tags, false)
			s.questions[question.Slug] = id
			outcome = result
			return err
		}); err != nil {
			return err
		}
		s.console.Info("%s", outcome)
	}
	return nil
}

func (s *contestRun) seedContestants(ctx context.Context) error {
	return s.console.Run(ctx, fmt.Sprintf("Sign up and log in %d contestants", len(s.catalog.Users)), func(ctx context.Context, w io.Writer) error {
		for _, user := range s.catalog.Users {
			id, token, err := s.lookupContestant(ctx, user.Username)
			if err != nil {
				return err
			}
			outcome := "exists"
			if id == "" {
				outcome = "signed up"
				if _, err := NewAPI(s.gateway).CreateUser(ctx, NewUser{
					Name: user.Name, Username: user.Username, Email: user.Username + "@" + ContestantDomain, Password: s.options.ContestantPassword,
				}); err != nil {
					return err
				}
				if id, token, err = s.lookupContestant(ctx, user.Username); err != nil {
					return err
				}
				if id == "" {
					return fmt.Errorf("%s is missing from the auth database after signing up", user.Username)
				}
			}
			if token != "" {
				if err := NewAPI(s.gateway).VerifyUser(ctx, id, token); err != nil {
					return err
				}
				outcome += ", verified"
			}
			api := NewAPI(s.gateway)
			if _, err := api.Login(ctx, user.Username, s.options.ContestantPassword); err != nil {
				return fmt.Errorf("%s: %w (an existing contestant must use the profile's contestant password)", user.Username, err)
			}
			s.users[user.Username] = &contestant{id: id, api: api}
			fmt.Fprintf(w, "%s: %s\n", user.Username, outcome)
		}
		return nil
	})
}

func (s *contestRun) lookupContestant(ctx context.Context, username string) (string, string, error) {
	out, err := s.postgres.Query(ctx, "auth", contestantQuery, map[string]string{"username": username})
	if err != nil {
		return "", "", err
	}
	line := strings.TrimSpace(out)
	if line == "" {
		return "", "", nil
	}
	fields := strings.Split(line, "|")
	if len(fields) != contestantColumns {
		return "", "", fmt.Errorf("unexpected auth database answer %q", out)
	}
	return fields[0], fields[1], nil
}

func transient(err error) bool {
	var apiErr *APIError
	if !errors.As(err, &apiErr) {
		return !errors.Is(err, context.Canceled)
	}
	return apiErr.Status >= http.StatusInternalServerError
}

func waitFor(ctx context.Context, timeout time.Duration, poll time.Duration, what string, check func() (bool, error)) error {
	deadline := time.Now().Add(timeout)
	var last error
	for {
		done, err := check()
		if err != nil && !transient(err) {
			return err
		}
		if err == nil && done {
			return nil
		}
		last = err
		if time.Now().After(deadline) {
			if last != nil {
				return fmt.Errorf("timed out after %s waiting for %s: %w", timeout, what, last)
			}
			return fmt.Errorf("timed out after %s waiting for %s", timeout, what)
		}
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-time.After(poll):
		}
	}
}

type judged struct {
	entry      ScriptEntry
	submission *Submission
}

func (s *contestRun) schedule(contest *Contest) (time.Time, time.Time) {
	now := time.Now().UTC()
	if contest.Upcoming() {
		liveAt := now.Add(time.Duration(contest.Meta.StartOffsetMinutes) * time.Minute)
		return liveAt, liveAt.Add(time.Duration(contest.Meta.DurationMinutes) * time.Minute)
	}
	return now.Add(OpenLead), now.Add(LiveWindow)
}

func (s *contestRun) seedContest(ctx context.Context, contest *Contest) error {
	s.console.Section("Contest " + contest.Meta.Name)
	request := NewContest{Name: contest.Meta.Name, Description: contest.Meta.Description}
	for _, question := range contest.Meta.Questions {
		request.Questions = append(request.Questions, NewContestQuestion{ID: s.questions[question.Slug], Points: question.Points})
	}
	var contestID string
	if err := s.console.Run(ctx, "Create the contest", func(ctx context.Context, w io.Writer) error {
		return waitFor(ctx, SyncWait, SyncPoll, "contest-service to receive the questions", func() (bool, error) {
			request.LiveAt, request.EndAt = s.schedule(contest)
			id, err := s.admin.CreateContest(ctx, request)
			if HasCode(err, "QUESTIONS_NOT_FOUND") {
				return false, nil
			}
			contestID = id
			return err == nil, err
		})
	}); err != nil {
		return err
	}
	if err := s.populate(ctx, contest, contestID); err != nil {
		s.console.Info("%s is only half seeded: delete it in the admin or reset, then seed again", contest.Meta.Name)
		return err
	}
	if contest.Upcoming() {
		s.console.OK("%s opens %s", contest.Meta.Name, request.LiveAt.Local().Format(time.RFC1123))
	}
	return nil
}

func (s *contestRun) populate(ctx context.Context, contest *Contest, contestID string) error {
	if err := s.console.Run(ctx, fmt.Sprintf("Register %d contestants", len(contest.Meta.Registrations)), func(ctx context.Context, w io.Writer) error {
		for _, username := range contest.Meta.Registrations {
			api := s.users[username].api
			if err := waitFor(ctx, SyncWait, SyncPoll, "contest-service to receive "+username, func() (bool, error) {
				err := api.Register(ctx, contestID)
				switch {
				case HasCode(err, "REGISTRATION_USER_NOT_FOUND"):
					return false, nil
				case HasCode(err, "ALREADY_REGISTERED"):
					return true, nil
				}
				return err == nil, err
			}); err != nil {
				return err
			}
		}
		return nil
	}); err != nil {
		return err
	}
	if contest.Upcoming() {
		return nil
	}
	if err := s.console.Run(ctx, "Wait for the contest to open its questions", func(ctx context.Context, w io.Writer) error {
		for _, question := range contest.Meta.Questions {
			id := s.questions[question.Slug]
			if err := waitFor(ctx, SyncWait, SyncPoll, question.Slug+" to become visible", func() (bool, error) {
				return s.admin.QuestionVisible(ctx, id)
			}); err != nil {
				return err
			}
		}
		return nil
	}); err != nil {
		return err
	}
	results, err := s.judge(ctx, contest)
	if err != nil {
		return err
	}
	if err := s.shift(ctx, contest, contestID, results); err != nil {
		return err
	}
	return s.showLeaderboard(ctx, contestID)
}

func (s *contestRun) judge(ctx context.Context, contest *Contest) ([]judged, error) {
	results := make([]judged, 0, len(contest.Script))
	if err := s.console.Run(ctx, fmt.Sprintf("Submit and judge %d scripted submissions", len(contest.Script)), func(ctx context.Context, w io.Writer) error {
		for _, entry := range contest.Script {
			code := s.catalog.code[s.catalog.variantPath(entry.Question, entry.Variant)]
			submission, err := s.users[entry.User].api.Submit(ctx, s.questions[entry.Question], code)
			if err != nil {
				return fmt.Errorf("%s on %s: %w", entry.User, entry.Question, err)
			}
			results = append(results, judged{entry: entry, submission: submission})
		}
		return waitFor(ctx, JudgeWait, JudgePoll, "the runner to judge every submission", func() (bool, error) {
			pending := 0
			for index := range results {
				if !slices.Contains(pendingStates, results[index].submission.State) {
					continue
				}
				submission, err := s.admin.Submission(ctx, results[index].submission.ID)
				if err != nil {
					return false, err
				}
				results[index].submission = submission
				if slices.Contains(pendingStates, submission.State) {
					pending++
				}
			}
			fmt.Fprintf(w, "%d of %d judged\n", len(results)-pending, len(results))
			return pending == 0, nil
		})
	}); err != nil {
		return nil, err
	}
	var rows [][]string
	mismatches := 0
	for _, result := range results {
		expected := variantStates[result.entry.Variant]
		mark := "ok"
		if result.submission.State != expected {
			mark = "expected " + expected
			mismatches++
		}
		rows = append(rows, []string{strconv.Itoa(result.entry.Minute), result.entry.User, result.entry.Question, result.entry.Variant, result.submission.State, mark})
	}
	s.console.Table([]string{"Minute", "User", "Question", "Variant", "Verdict", "Check"}, rows)
	if mismatches > 0 {
		return nil, s.console.Errorf("%d verdict(s) differ from the script, so %s keeps its real times", mismatches, contest.Meta.Name)
	}
	return results, nil
}

type submissionTime struct {
	ID        string `json:"id"`
	CreatedAt string `json:"created_at"`
}

type registrationTime struct {
	UserID       string `json:"user_id"`
	RegisteredAt string `json:"registered_at"`
}

type expectedState struct {
	ID    string `json:"id"`
	State string `json:"state"`
}

func timestamp(at time.Time) string {
	return at.UTC().Format(time.RFC3339Nano)
}

func (s *contestRun) shift(ctx context.Context, contest *Contest, contestID string, results []judged) error {
	expected := make([]expectedState, 0, len(results))
	for _, result := range results {
		expected = append(expected, expectedState{ID: result.submission.ID, State: result.submission.State})
	}
	expectedJSON, err := json.Marshal(expected)
	if err != nil {
		return err
	}
	if err := s.console.Run(ctx, "Wait for contest-service to receive every verdict", func(ctx context.Context, w io.Writer) error {
		return waitFor(ctx, SyncWait, SyncPoll, "the submission verdicts to reach contest-service", func() (bool, error) {
			out, err := s.postgres.Query(ctx, "contest", syncedSubmissionsQuery, map[string]string{"expected": string(expectedJSON)})
			if err != nil {
				return false, err
			}
			count, err := strconv.Atoi(strings.TrimSpace(out))
			if err != nil {
				return false, fmt.Errorf("unexpected contest database answer %q", out)
			}
			return count == len(expected), nil
		})
	}); err != nil {
		return err
	}
	liveAt := time.Now().UTC().Truncate(time.Microsecond).Add(time.Duration(contest.Meta.StartOffsetMinutes) * time.Minute)
	endAt := liveAt.Add(time.Duration(contest.Meta.DurationMinutes) * time.Minute)
	submissions := make([]submissionTime, 0, len(results))
	for _, result := range results {
		at := liveAt.Add(time.Duration(result.entry.Minute) * time.Minute)
		submissions = append(submissions, submissionTime{ID: result.submission.ID, CreatedAt: timestamp(at)})
	}
	registrations := make([]registrationTime, 0, len(contest.Meta.Registrations))
	for index, username := range contest.Meta.Registrations {
		at := liveAt.Add(-RegistrationLead + time.Duration(index)*RegistrationSpacing)
		registrations = append(registrations, registrationTime{UserID: s.users[username].id, RegisteredAt: timestamp(at)})
	}
	submissionsJSON, err := json.Marshal(submissions)
	if err != nil {
		return err
	}
	registrationsJSON, err := json.Marshal(registrations)
	if err != nil {
		return err
	}
	vars := map[string]string{
		"contest_id":         contestID,
		"live_at":            timestamp(liveAt),
		"end_at":             timestamp(endAt),
		"created_at":         timestamp(liveAt.Add(-CreationLead)),
		"submissions":        string(submissionsJSON),
		"submission_count":   strconv.Itoa(len(submissions)),
		"registrations":      string(registrationsJSON),
		"registration_count": strconv.Itoa(len(registrations)),
	}
	if err := s.console.Run(ctx, "Move the problem-service copy to its real schedule", func(ctx context.Context, w io.Writer) error {
		return s.runShift(ctx, "problem", problemShift, vars)
	}); err != nil {
		return err
	}
	return s.console.Run(ctx, "Move the contest to its real schedule and rebuild the leaderboard", func(ctx context.Context, w io.Writer) error {
		return s.runShift(ctx, "contest", contestShift, vars)
	})
}

func (s *contestRun) runShift(ctx context.Context, database string, script string, vars map[string]string) error {
	out, err := s.postgres.Query(ctx, database, script, vars)
	if err != nil {
		return err
	}
	if strings.TrimSpace(out) != ShiftedMarker {
		return fmt.Errorf("unexpected %s database answer %q", database, out)
	}
	return nil
}

func (s *contestRun) showLeaderboard(ctx context.Context, contestID string) error {
	out, err := s.postgres.Query(ctx, "contest", leaderboardQuery, map[string]string{"contest_id": contestID})
	if err != nil {
		return s.console.Errorf("reading the leaderboard: %v", err)
	}
	var rows [][]string
	for _, line := range strings.Split(strings.TrimSpace(out), "\n") {
		if fields := strings.Split(line, "|"); len(fields) == leaderboardColumns {
			rows = append(rows, fields)
		}
	}
	if len(rows) == 0 {
		return s.console.Errorf("the leaderboard is empty: %w", errors.New(strings.TrimSpace(out)))
	}
	s.console.Table([]string{"Rank", "User", "Points", "Solved"}, rows)
	s.console.OK("Leaderboard rebuilt")
	return nil
}
