# Problem Contracts

The problem service's shared types: permission names, change events, and the submission language and state enums.

## Overview

- `ProblemPermissions` lists the problem service's permissions. The auth service registers them so roles can hold them.
- **Events** published by the problem service:
  - `QuestionCdcEventData`: a question's visibility and limits
  - `TestCaseCdcEventData`: a test case's input and expected output
  - `SubmissionCdcEventData`: a submission's code, language, state and results
- `Language`: the judged languages, C++, Java and Python.
- `SubmissionState`: every state a submission can take, from `queued` through `compiled` to the verdicts and `runner_error`.
