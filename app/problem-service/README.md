# Problem Service

Owns the question bank and submissions: questions, test cases, tags, categories and the code users submit. It creates submissions and records the verdicts coming back from the runner.

## Overview

- **Questions** have a title, summary, description, a visibility flag, and their own time and memory limits.
- **Test cases** belong to a question and are public or private.
- **Submissions** start as `queued` and move through `compiled` to a verdict as the runner reports back on `runner.submission.state`. Each event is applied only from a valid previous state, so duplicates and late events change nothing.
- Questions, test cases and submissions are published through the outbox, so the runner and contest service can keep copies.
- It keeps its own copies of users (for usernames) and contests. When a contest opens, its private questions become visible and can't be hidden or deleted until it ends.
- Data lives in the `problem` database.

## API

Request and response shapes are in Swagger UI on the gateway. Private questions and private test cases are hidden from callers without the matching `read_is_private` permission.

### Questions

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/questions` | List questions, filter by title, category, tag, visibility, dates | — (private ones need `problem:question:read_is_private`) |
| GET | `/api/v1/questions/{ID}` | Get a question | — (private needs `problem:question:read_is_private`) |
| POST | `/api/v1/questions` | Create a question with limits, categories and tags | `problem:question:write` (+ `problem:question:write_is_private` for a private one) |
| PUT | `/api/v1/questions/{ID}` | Replace a question, or create it under that id | `problem:question:write` (+ `write_is_private` when private) |
| PATCH | `/api/v1/questions/{ID}` | Update some fields | `problem:question:write` (+ `write_is_private` when private) |
| DELETE | `/api/v1/questions/{ID}` | Delete a question; refused while it is in a running contest | `problem:question:write` (+ `write_is_private` when private) |

### Test cases

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/questions/{QuestionID}/test-cases` | List a question's test cases | — (private ones need `problem:test_case:read_is_private`) |
| GET | `/api/v1/questions/{QuestionID}/test-cases/{ID}` | Get a test case | — (private needs `problem:test_case:read_is_private`) |
| POST | `/api/v1/questions/{QuestionID}/test-cases` | Add an input and expected output | `problem:test_case:write` (+ `problem:test_case:write_is_private` for a private one) |
| PUT | `/api/v1/questions/{QuestionID}/test-cases/{ID}` | Replace a test case, or create it under that id | `problem:test_case:write` (+ `write_is_private` when private) |
| PATCH | `/api/v1/questions/{QuestionID}/test-cases/{ID}` | Update some fields | `problem:test_case:write` (+ `write_is_private` when private) |
| DELETE | `/api/v1/questions/{QuestionID}/test-cases/{ID}` | Delete a test case | `problem:test_case:write` (+ `write_is_private` when private) |

Test cases of a private question also need `problem:question:read_is_private`.

### Submissions

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/submissions` | List submissions, filter by question, user id, username, language, state, dates | — |
| GET | `/api/v1/submissions/{ID}` | Get a submission with its state, failed test case, time, memory, compiler output and the program's output on a failed public test | — |
| POST | `/api/v1/submissions` | Submit code in `cpp`, `java` or `python` | `problem:submission:write` |
| DELETE | `/api/v1/submissions/{ID}` | Delete a submission | `problem:submission:delete` / `problem:submission:delete_own` |

Submissions to private questions need `problem:question:read_is_private` to read or create.

### Tags and categories

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/tags`, `/api/v1/categories` | List | — |
| GET | `/api/v1/tags/{ID}`, `/api/v1/categories/{ID}` | Get one | — |
| POST | `/api/v1/tags`, `/api/v1/categories` | Create | `problem:tag:write` / `problem:category:write` |
| PUT | `/api/v1/tags/{ID}`, `/api/v1/categories/{ID}` | Replace, or create under that id | `problem:tag:write` / `problem:category:write` |
| PATCH | `/api/v1/tags/{ID}`, `/api/v1/categories/{ID}` | Update some fields | `problem:tag:write` / `problem:category:write` |
| DELETE | `/api/v1/tags/{ID}`, `/api/v1/categories/{ID}` | Delete and unlink from questions | `problem:tag:write` / `problem:category:write` |
