package seed

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

const (
	APIPrefix      = "/api/v1"
	RequestTimeout = 60 * time.Second
	PageSize       = 100
	LoginWait      = 2 * time.Minute
	LoginPoll      = 3 * time.Second

	VerificationTokenHeader = "X-EMAIL-VERIFICATION-TOKEN"
)

type API struct {
	base   string
	token  string
	client *http.Client
}

type APIError struct {
	Action string
	Status int
	Code   string
	Detail string
}

func (e *APIError) Error() string {
	if e.Code == "" {
		return fmt.Sprintf("%s failed with HTTP %d", e.Action, e.Status)
	}
	return fmt.Sprintf("%s failed with HTTP %d - %s: %s", e.Action, e.Status, e.Code, e.Detail)
}

func NewAPI(base string) *API {
	return &API{base: strings.TrimSuffix(base, "/") + APIPrefix, client: &http.Client{Timeout: RequestTimeout}}
}

func (a *API) do(ctx context.Context, action string, method string, path string, body any, out any, accepted ...int) (int, error) {
	return a.doWith(ctx, action, method, path, nil, body, out, accepted...)
}

func (a *API) doWith(ctx context.Context, action string, method string, path string, headers map[string]string, body any, out any, accepted ...int) (int, error) {
	var reader io.Reader
	if body != nil {
		data, err := json.Marshal(body)
		if err != nil {
			return 0, err
		}
		reader = bytes.NewReader(data)
	}
	request, err := http.NewRequestWithContext(ctx, method, a.base+path, reader)
	if err != nil {
		return 0, err
	}
	request.Header.Set("Accept", "application/json")
	if body != nil {
		request.Header.Set("Content-Type", "application/json")
	}
	if a.token != "" {
		request.Header.Set("Authorization", "Bearer "+a.token)
	}
	for name, value := range headers {
		request.Header.Set(name, value)
	}
	response, err := a.client.Do(request)
	if err != nil {
		return 0, fmt.Errorf("cannot reach %s: %w", a.base, err)
	}
	defer response.Body.Close()
	data, err := io.ReadAll(response.Body)
	if err != nil {
		return response.StatusCode, err
	}
	for _, status := range accepted {
		if response.StatusCode == status {
			if out != nil && len(data) > 0 {
				if err := json.Unmarshal(data, out); err != nil {
					return response.StatusCode, fmt.Errorf("%s: unreadable response: %w", action, err)
				}
			}
			return response.StatusCode, nil
		}
	}
	apiErr := &APIError{Action: action, Status: response.StatusCode}
	var problem struct {
		Code       string          `json:"code"`
		Message    string          `json:"message"`
		Properties json.RawMessage `json:"properties"`
	}
	if json.Unmarshal(data, &problem) == nil {
		apiErr.Code = problem.Code
		apiErr.Detail = problem.Message
		var properties struct {
			Permissions []string `json:"permissions"`
		}
		if json.Unmarshal(problem.Properties, &properties) == nil && len(properties.Permissions) > 0 {
			apiErr.Detail += " (missing: " + strings.Join(properties.Permissions, ", ") + ")"
		}
	}
	return response.StatusCode, apiErr
}

type session struct {
	ID          string   `json:"id"`
	Permissions []string `json:"permissions"`
}

func (a *API) Login(ctx context.Context, username string, password string) ([]string, error) {
	var created session
	var status int
	var err error
	deadline := time.Now().Add(LoginWait)
	for {
		status, err = a.do(ctx, "login", http.MethodPost, "/sessions", map[string]string{"username": username, "password": password}, &created, http.StatusCreated)
		retryable := status == 0 || status >= http.StatusInternalServerError
		if err == nil || !retryable || time.Now().After(deadline) {
			break
		}
		select {
		case <-ctx.Done():
			return nil, ctx.Err()
		case <-time.After(LoginPoll):
		}
	}
	switch {
	case status == http.StatusUnauthorized:
		return nil, fmt.Errorf("login failed: wrong username or password for %s, run seed admin first", username)
	case status == http.StatusConflict:
		return nil, fmt.Errorf("login failed: %s has not verified their email", username)
	case err != nil:
		return nil, err
	}
	a.token = created.ID
	return created.Permissions, nil
}

type named struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type page[T any] struct {
	Data    []T  `json:"data"`
	HasNext bool `json:"hasNext"`
}

func (a *API) FindNamed(ctx context.Context, resource string, name string) (string, error) {
	for number := 1; ; number++ {
		var result page[named]
		path := fmt.Sprintf("/%s?name=%s&size=%d&page=%d", resource, url.QueryEscape(name), PageSize, number)
		if _, err := a.do(ctx, "listing "+resource, http.MethodGet, path, nil, &result, http.StatusOK); err != nil {
			return "", err
		}
		for _, item := range result.Data {
			if strings.EqualFold(item.Name, name) {
				return item.ID, nil
			}
		}
		if !result.HasNext {
			return "", nil
		}
	}
}

func (a *API) CreateNamed(ctx context.Context, resource string, body json.RawMessage) (string, bool, error) {
	var created named
	status, err := a.do(ctx, "creating "+resource, http.MethodPost, "/"+resource, body, &created, http.StatusCreated, http.StatusConflict)
	if err != nil {
		return "", false, err
	}
	return created.ID, status == http.StatusCreated, nil
}

type questionSummary struct {
	ID    string `json:"id"`
	Title string `json:"title"`
}

func (a *API) FindQuestion(ctx context.Context, title string) (string, error) {
	for number := 1; ; number++ {
		var result page[questionSummary]
		path := fmt.Sprintf("/questions?title=%s&size=%d&page=%d", url.QueryEscape(title), PageSize, number)
		if _, err := a.do(ctx, "listing questions", http.MethodGet, path, nil, &result, http.StatusOK); err != nil {
			return "", err
		}
		for _, item := range result.Data {
			if item.Title == title {
				return item.ID, nil
			}
		}
		if !result.HasNext {
			return "", nil
		}
	}
}

type NewQuestion struct {
	Title         string   `json:"title"`
	Summary       string   `json:"summary"`
	Description   string   `json:"description"`
	IsVisible     bool     `json:"isVisible"`
	MaxRunTimeMs  int      `json:"maxRunTimeMs"`
	MaxRamLimitMb int      `json:"maxRamLimitMb"`
	Categories    []string `json:"categories"`
	Tags          []string `json:"tags"`
}

func (a *API) CreateQuestion(ctx context.Context, question NewQuestion) (string, error) {
	var created questionSummary
	_, err := a.do(ctx, "creating question "+question.Title, http.MethodPost, "/questions", question, &created, http.StatusCreated)
	return created.ID, err
}

func (a *API) CountTests(ctx context.Context, questionID string) (int, error) {
	var result struct {
		Items int `json:"items"`
	}
	_, err := a.do(ctx, "counting test cases", http.MethodGet, "/questions/"+questionID+"/test-cases?size=1", nil, &result, http.StatusOK)
	return result.Items, err
}

func (a *API) CreateTest(ctx context.Context, questionID string, test Test) error {
	body := map[string]any{"input": test.Input, "output": test.Output, "isPublic": true}
	_, err := a.do(ctx, "uploading a test case", http.MethodPost, "/questions/"+questionID+"/test-cases", body, nil, http.StatusCreated)
	return err
}

type Submission struct {
	ID           string `json:"id"`
	State        string `json:"state"`
	TimeTakenMs  *int   `json:"timeTakenMs"`
	MemoryUsedKb *int   `json:"memoryUsedKb"`
}

func (a *API) Submit(ctx context.Context, questionID string, code string) (*Submission, error) {
	var created Submission
	body := map[string]string{"questionId": questionID, "language": "cpp", "code": code}
	_, err := a.do(ctx, "submitting", http.MethodPost, "/submissions", body, &created, http.StatusCreated)
	return &created, err
}

func (a *API) Submission(ctx context.Context, id string) (*Submission, error) {
	var submission Submission
	_, err := a.do(ctx, "polling a submission", http.MethodGet, "/submissions/"+id, nil, &submission, http.StatusOK)
	return &submission, err
}

func HasCode(err error, code string) bool {
	var apiErr *APIError
	if !errors.As(err, &apiErr) {
		return false
	}
	return apiErr.Code == code || strings.HasSuffix(apiErr.Code, "."+code)
}

type NewUser struct {
	Name     string `json:"name"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

func (a *API) CreateUser(ctx context.Context, user NewUser) (string, error) {
	var created struct {
		ID string `json:"id"`
	}
	_, err := a.do(ctx, "signing up "+user.Username, http.MethodPost, "/users", user, &created, http.StatusCreated)
	return created.ID, err
}

func (a *API) VerifyUser(ctx context.Context, id string, token string) error {
	headers := map[string]string{VerificationTokenHeader: token}
	_, err := a.doWith(ctx, "verifying a user", http.MethodPatch, "/users/"+id, headers, map[string]bool{"verified": true}, nil, http.StatusOK)
	return err
}

func (a *API) QuestionVisible(ctx context.Context, id string) (bool, error) {
	var question struct {
		IsVisible bool `json:"isVisible"`
	}
	_, err := a.do(ctx, "reading a question", http.MethodGet, "/questions/"+id, nil, &question, http.StatusOK)
	return question.IsVisible, err
}

func (a *API) FindContest(ctx context.Context, name string) (string, error) {
	for number := 1; ; number++ {
		var result page[named]
		path := fmt.Sprintf("/contests?name=%s&size=%d&page=%d", url.QueryEscape(name), PageSize, number)
		if _, err := a.do(ctx, "listing contests", http.MethodGet, path, nil, &result, http.StatusOK); err != nil {
			return "", err
		}
		for _, item := range result.Data {
			if strings.EqualFold(item.Name, name) {
				return item.ID, nil
			}
		}
		if !result.HasNext {
			return "", nil
		}
	}
}

type NewContestQuestion struct {
	ID     string `json:"id"`
	Points int    `json:"points"`
}

type NewContest struct {
	Name        string               `json:"name"`
	Description string               `json:"description"`
	LiveAt      time.Time            `json:"liveAt"`
	EndAt       time.Time            `json:"endAt"`
	Questions   []NewContestQuestion `json:"questions"`
}

func (a *API) CreateContest(ctx context.Context, contest NewContest) (string, error) {
	var created named
	_, err := a.do(ctx, "creating contest "+contest.Name, http.MethodPost, "/contests", contest, &created, http.StatusCreated)
	return created.ID, err
}

func (a *API) Register(ctx context.Context, contestID string) error {
	_, err := a.do(ctx, "registering", http.MethodPost, "/contests/"+contestID+"/registration", nil, nil, http.StatusOK, http.StatusCreated, http.StatusNoContent)
	return err
}
