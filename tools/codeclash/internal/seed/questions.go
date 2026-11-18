package seed

import (
	"archive/tar"
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"slices"
	"strconv"
	"strings"
	"time"

	"codeclash/internal/execx"
	"codeclash/internal/images"
	"codeclash/internal/repo"
	"codeclash/internal/ui"
)

const (
	MaxTestBytes      = 1 << 20
	RunTimeoutSeconds = 60
	PidsLimit         = 512
	ArchiveFileMode   = 0o644
	VerifyTimeout     = 120 * time.Second
	VerifyPoll        = time.Second
	planFile          = "plan.txt"
	testsDir          = "tests"
)

const generateScript = `set -euo pipefail
shopt -s nullglob
exec 3>&1 1>&2
mkdir -p /work && cd /work && tar -x <&0
mkdir -p bin tests
for q in questions/*/; do
  slug=$(basename "$q")
  printf 'gen %s\nsol %s\n' "$slug" "$slug"
done | xargs -P "$(nproc)" -n 2 sh -c '
  if [ "$0" = gen ]; then
    g++ -std=c++20 -O2 -I generators -o "bin/gen-$1" "generators/$1.cpp"
  else
    g++ -std=c++20 -O2 -o "bin/sol-$1" "solutions/$1.cpp"
  fi'
while read -r slug count; do
  dir="tests/$slug"
  mkdir -p "$dir"
  n=0
  for sample in $(printf '%s\n' questions/$slug/samples/*.in | sort -V); do
    n=$((n + 1))
    cp "$sample" "$dir/$n.in"
  done
  for ((i = 1; i <= count; i++)); do
    n=$((n + 1))
    echo "generating $slug test $n"
    timeout ` + "${RUN_TIMEOUT}" + ` "bin/gen-$slug" "$i" < /dev/null > "$dir/$n.in"
  done
  for ((i = 1; i <= n; i++)); do
    echo "solving $slug test $i"
    timeout ` + "${RUN_TIMEOUT}" + ` "bin/sol-$slug" < "$dir/$i.in" > "$dir/$i.out"
  done
done < ` + planFile + `
tar -c tests >&3
`

var pendingStates = []string{"queued", "compiled"}

type Meta struct {
	Title          string   `json:"title"`
	Summary        string   `json:"summary"`
	MaxRunTimeMs   int      `json:"maxRunTimeMs"`
	MaxRamLimitMb  int      `json:"maxRamLimitMb"`
	Categories     []string `json:"categories"`
	Tags           []string `json:"tags"`
	GeneratedTests int      `json:"generatedTests"`
}

type Test struct {
	Input  string
	Output string
}

type Question struct {
	Slug      string
	Dir       string
	Meta      Meta
	Statement string
	Solution  string
	Tests     []Test
}

type Catalog struct {
	Dir        string
	Categories []json.RawMessage
	Tags       []json.RawMessage
	Questions  []*Question
}

func LoadCatalog(r *repo.Repo) (*Catalog, error) {
	catalog, err := loadNamedCatalog(r.Path("seed"))
	if err != nil {
		return nil, err
	}
	if catalog.Questions, err = loadQuestions(catalog.Dir, filepath.Join(catalog.Dir, "questions")); err != nil {
		return nil, err
	}
	return catalog, nil
}

func loadNamedCatalog(dir string) (*Catalog, error) {
	catalog := &Catalog{Dir: dir}
	for file, target := range map[string]*[]json.RawMessage{"categories.json": &catalog.Categories, "tags.json": &catalog.Tags} {
		data, err := os.ReadFile(filepath.Join(dir, "catalog", file))
		if err != nil {
			return nil, err
		}
		if err := json.Unmarshal(data, target); err != nil {
			return nil, fmt.Errorf("catalog/%s: %w", file, err)
		}
	}
	return catalog, nil
}

func loadQuestions(dir string, questionsDir string) ([]*Question, error) {
	entries, err := os.ReadDir(questionsDir)
	if err != nil {
		return nil, err
	}
	var questions []*Question
	for _, entry := range entries {
		if !entry.IsDir() {
			continue
		}
		question, err := loadQuestion(dir, filepath.Join(questionsDir, entry.Name()), entry.Name())
		if err != nil {
			return nil, err
		}
		questions = append(questions, question)
	}
	return questions, nil
}

func loadQuestion(dir string, questionDir string, slug string) (*Question, error) {
	question := &Question{Slug: slug, Dir: questionDir}
	data, err := os.ReadFile(filepath.Join(questionDir, "question.json"))
	if err != nil {
		return nil, err
	}
	if err := json.Unmarshal(data, &question.Meta); err != nil {
		return nil, fmt.Errorf("%s/question.json: %w", slug, err)
	}
	statement, err := os.ReadFile(filepath.Join(questionDir, "statement.md"))
	if err != nil {
		return nil, err
	}
	question.Statement = string(statement)
	solution, err := os.ReadFile(filepath.Join(dir, "solutions", slug+".cpp"))
	if err != nil {
		return nil, fmt.Errorf("missing solutions/%s.cpp: %w", slug, err)
	}
	question.Solution = string(solution)
	if _, err := os.Stat(filepath.Join(dir, "generators", slug+".cpp")); err != nil {
		return nil, fmt.Errorf("missing generators/%s.cpp: %w", slug, err)
	}
	return question, nil
}

func (c *Catalog) archive() (*bytes.Buffer, error) {
	var buffer bytes.Buffer
	writer := tar.NewWriter(&buffer)
	add := func(name string, data []byte) error {
		header := &tar.Header{Name: name, Mode: ArchiveFileMode, Size: int64(len(data)), ModTime: time.Unix(0, 0)}
		if err := writer.WriteHeader(header); err != nil {
			return err
		}
		_, err := writer.Write(data)
		return err
	}
	walk := func(root string, prefix string) error {
		return filepath.WalkDir(root, func(path string, entry os.DirEntry, err error) error {
			if err != nil || entry.IsDir() {
				return err
			}
			relative, err := filepath.Rel(root, path)
			if err != nil {
				return err
			}
			data, err := os.ReadFile(path)
			if err != nil {
				return err
			}
			return add(prefix+"/"+filepath.ToSlash(relative), data)
		})
	}
	for _, sub := range []string{"generators", "solutions"} {
		if err := walk(filepath.Join(c.Dir, sub), sub); err != nil {
			return nil, err
		}
	}
	for _, question := range c.Questions {
		if err := walk(question.Dir, "questions/"+question.Slug); err != nil {
			return nil, err
		}
	}
	var plan strings.Builder
	for _, question := range c.Questions {
		fmt.Fprintf(&plan, "%s %d\n", question.Slug, question.Meta.GeneratedTests)
	}
	if err := add(planFile, []byte(plan.String())); err != nil {
		return nil, err
	}
	return &buffer, writer.Close()
}

func (c *Catalog) Generate(ctx context.Context, w io.Writer) error {
	input, err := c.archive()
	if err != nil {
		return err
	}
	var output bytes.Buffer
	script := strings.ReplaceAll(generateScript, "${RUN_TIMEOUT}", strconv.Itoa(RunTimeoutSeconds))
	cmd := execx.Command("docker", "run", "--rm", "-i", "--init", "--network", "none", "--pids-limit", strconv.Itoa(PidsLimit),
		"--entrypoint", "bash", images.SandboxImage, "-c", script).Stdin(input)
	if err := cmd.Pipe(ctx, &output, w); err != nil {
		return err
	}
	tests := map[string]map[int]*Test{}
	reader := tar.NewReader(&output)
	for {
		header, err := reader.Next()
		if errors.Is(err, io.EOF) {
			break
		}
		if err != nil {
			return err
		}
		if header.Typeflag != tar.TypeReg {
			continue
		}
		parts := strings.Split(strings.TrimPrefix(header.Name, "./"), "/")
		if len(parts) != 3 || parts[0] != testsDir {
			continue
		}
		slug, file := parts[1], parts[2]
		base, ext := strings.TrimSuffix(file, filepath.Ext(file)), filepath.Ext(file)
		index, err := strconv.Atoi(base)
		if err != nil {
			continue
		}
		if header.Size > MaxTestBytes {
			return fmt.Errorf("%s is %d bytes, over the %d byte limit", header.Name, header.Size, MaxTestBytes)
		}
		data, err := io.ReadAll(reader)
		if err != nil {
			return err
		}
		if tests[slug] == nil {
			tests[slug] = map[int]*Test{}
		}
		if tests[slug][index] == nil {
			tests[slug][index] = &Test{}
		}
		switch ext {
		case ".in":
			tests[slug][index].Input = string(data)
		case ".out":
			tests[slug][index].Output = string(data)
		}
	}
	for _, question := range c.Questions {
		generated := tests[question.Slug]
		samples, err := filepath.Glob(filepath.Join(question.Dir, "samples", "*.in"))
		if err != nil {
			return err
		}
		if expected := len(samples) + question.Meta.GeneratedTests; len(generated) != expected {
			return fmt.Errorf("%s has %d generated tests, expected %d", question.Slug, len(generated), expected)
		}
		question.Tests = make([]Test, 0, len(generated))
		for index := 1; index <= len(generated); index++ {
			test, ok := generated[index]
			if !ok {
				return fmt.Errorf("%s is missing test %d", question.Slug, index)
			}
			question.Tests = append(question.Tests, *test)
		}
	}
	return nil
}

func ensureNamed(ctx context.Context, w io.Writer, api *API, resource string, entries []json.RawMessage) (map[string]string, error) {
	ids := map[string]string{}
	for _, entry := range entries {
		var item struct {
			Name string `json:"name"`
		}
		if err := json.Unmarshal(entry, &item); err != nil || item.Name == "" {
			return nil, fmt.Errorf("a %s entry has no name", resource)
		}
		id, err := api.FindNamed(ctx, resource, item.Name)
		if err != nil {
			return nil, err
		}
		if id == "" {
			created, fresh, err := api.CreateNamed(ctx, resource, entry)
			if err != nil {
				return nil, err
			}
			if fresh {
				id = created
				fmt.Fprintf(w, "created %s: %s\n", resource, item.Name)
			} else if id, err = api.FindNamed(ctx, resource, item.Name); err != nil {
				return nil, err
			}
		} else {
			fmt.Fprintf(w, "exists  %s: %s\n", resource, item.Name)
		}
		if id == "" {
			return nil, fmt.Errorf("could not resolve %s %s", resource, item.Name)
		}
		ids[item.Name] = id
	}
	return ids, nil
}

func lookup(ids map[string]string, names []string, what string) ([]string, error) {
	out := make([]string, 0, len(names))
	for _, name := range names {
		id, ok := ids[name]
		if !ok {
			return nil, fmt.Errorf("unknown %s %s", what, name)
		}
		out = append(out, id)
	}
	return out, nil
}

type Options struct {
	Username string
	Password string
	Verify   bool
}

func Questions(ctx context.Context, console *ui.Console, r *repo.Repo, gateway string, options Options) error {
	console.Section("Questions")
	catalog, err := LoadCatalog(r)
	if err != nil {
		return console.Errorf("reading seed/: %v", err)
	}
	if !images.Present(ctx, images.SandboxImage) {
		if err := console.Run(ctx, "Build "+images.SandboxImage, func(ctx context.Context, w io.Writer) error {
			return images.BuildSandbox(ctx, w, r)
		}); err != nil {
			return err
		}
	}
	if err := console.Run(ctx, fmt.Sprintf("Generate tests for %d questions in %s", len(catalog.Questions), images.SandboxImage), catalog.Generate); err != nil {
		return err
	}
	api := NewAPI(gateway)
	if err := console.Run(ctx, "Log in as "+options.Username, func(ctx context.Context, w io.Writer) error {
		permissions, err := api.Login(ctx, options.Username, options.Password)
		if err != nil {
			return err
		}
		required := []string{"problem:category:write", "problem:tag:write", "problem:question:write", "problem:test_case:write"}
		if options.Verify {
			required = append(required, "problem:submission:write")
		}
		for _, permission := range required {
			if !slices.Contains(permissions, permission) {
				return fmt.Errorf("%s lacks %s, give the account the admin or superadmin role", options.Username, permission)
			}
		}
		return nil
	}); err != nil {
		return err
	}
	var categories, tags map[string]string
	if err := console.Run(ctx, "Ensure categories and tags", func(ctx context.Context, w io.Writer) error {
		var err error
		if categories, err = ensureNamed(ctx, w, api, "categories", catalog.Categories); err != nil {
			return err
		}
		tags, err = ensureNamed(ctx, w, api, "tags", catalog.Tags)
		return err
	}); err != nil {
		return err
	}
	ids := map[string]string{}
	for _, question := range catalog.Questions {
		var outcome string
		if err := console.Run(ctx, question.Meta.Title, func(ctx context.Context, w io.Writer) error {
			id, result, err := seedQuestion(ctx, api, question, categories, tags, true)
			ids[question.Slug] = id
			outcome = result
			return err
		}); err != nil {
			return err
		}
		console.Info("%s", outcome)
	}
	if options.Verify {
		return verify(ctx, console, api, catalog, ids)
	}
	return nil
}

func seedQuestion(ctx context.Context, api *API, question *Question, categories map[string]string, tags map[string]string, visible bool) (string, string, error) {
	id, err := api.FindQuestion(ctx, question.Meta.Title)
	if err != nil {
		return "", "", err
	}
	have := 0
	if id == "" {
		categoryIDs, err := lookup(categories, question.Meta.Categories, "category")
		if err != nil {
			return "", "", err
		}
		tagIDs, err := lookup(tags, question.Meta.Tags, "tag")
		if err != nil {
			return "", "", err
		}
		id, err = api.CreateQuestion(ctx, NewQuestion{
			Title:         question.Meta.Title,
			Summary:       question.Meta.Summary,
			Description:   question.Statement,
			IsVisible:     visible,
			MaxRunTimeMs:  question.Meta.MaxRunTimeMs,
			MaxRamLimitMb: question.Meta.MaxRamLimitMb,
			Categories:    categoryIDs,
			Tags:          tagIDs,
		})
		if err != nil {
			return "", "", err
		}
	} else if have, err = api.CountTests(ctx, id); err != nil {
		return id, "", err
	}
	if have >= len(question.Tests) {
		return id, "already seeded", nil
	}
	for index := have; index < len(question.Tests); index++ {
		if err := api.CreateTest(ctx, id, question.Tests[index]); err != nil {
			return id, "", fmt.Errorf("test %d: %w", index+1, err)
		}
	}
	if have == 0 {
		return id, fmt.Sprintf("created with %d test cases", len(question.Tests)), nil
	}
	return id, fmt.Sprintf("resumed, uploaded %d missing test cases", len(question.Tests)-have), nil
}

func verify(ctx context.Context, console *ui.Console, api *API, catalog *Catalog, ids map[string]string) error {
	console.Section("Verify reference solutions")
	var rows [][]string
	failed := 0
	for _, question := range catalog.Questions {
		var result *Submission
		if err := console.Run(ctx, "Judge "+question.Slug, func(ctx context.Context, w io.Writer) error {
			submission, err := api.Submit(ctx, ids[question.Slug], question.Solution)
			if err != nil {
				return err
			}
			deadline := time.Now().Add(VerifyTimeout)
			for slices.Contains(pendingStates, submission.State) {
				if time.Now().After(deadline) {
					submission.State = "timed out"
					break
				}
				select {
				case <-ctx.Done():
					return ctx.Err()
				case <-time.After(VerifyPoll):
				}
				if submission, err = api.Submission(ctx, submission.ID); err != nil {
					return err
				}
			}
			result = submission
			return nil
		}); err != nil {
			return err
		}
		if result.State != "correct" {
			failed++
		}
		rows = append(rows, []string{question.Slug, result.State, optional(result.TimeTakenMs, "ms"), optional(result.MemoryUsedKb, "KB")})
	}
	console.Table([]string{"Question", "Verdict", "Time", "Memory"}, rows)
	if failed > 0 {
		return console.Errorf("%d reference solution(s) were not accepted", failed)
	}
	console.OK("All reference solutions accepted")
	return nil
}

func optional(value *int, unit string) string {
	if value == nil {
		return "-"
	}
	return strconv.Itoa(*value) + " " + unit
}
