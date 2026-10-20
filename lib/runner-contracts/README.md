# Runner Contracts

The event the runner publishes when a submission's state changes.

## Overview

`SubmissionStateEventData` is the payload of `runner.submission.state`, keyed by submission id.

| Field | Set when |
| --- | --- |
| `submissionId`, `state` | always |
| `failedTestCaseId` | the submission failed on a test case |
| `timeTakenMs`, `memoryUsedKb` | `correct`: the largest time and memory across the test cases |
| `compileOutput` | `compilation_error`: compiler stdout and stderr, up to 64 KiB |
| `actualOutput` | `incorrect` or `runtime_error` on a public test case: the program's stdout, up to 65536 code points |

- Cut output ends with `… output truncated`.
- Hidden test case output is never sent, so private test data never leaves the runner.
- The runner publishes `compiled` once compilation succeeds, then one final verdict, or `runner_error` when judging can't finish.
- The problem service applies an event only when the submission's current state allows it, so duplicates and late events change nothing.
