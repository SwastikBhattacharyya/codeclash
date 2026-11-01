# Runner Service

Judges submitted code. It takes queued submissions from Kafka, compiles and runs them in Docker sandboxes against every test case, and publishes the verdict.

## Overview

- No HTTP API. It keeps copies of question limits and test cases from `problem.question.cdc` and `problem.test-case.cdc`.
- Queued submissions from `problem.submission.cdc` become rows in a job table.
- Up to `max-concurrent` workers (4 by default) claim jobs (`for update skip locked`, five-minute lease). A job is retried up to three times when a sandbox fails.
- Each job runs in a fresh container from a warm pool: no network, read-only filesystem, dropped capabilities, memory and CPU limits, and a seccomp filter. Containers are never reused.
- The time limit is multiplied per language (C++ 1×, Java 2×, Python 3×). Output is compared line by line, ignoring trailing whitespace.
- States are published on `runner.submission.state`. Data lives in the `runner` database.
- Needs the `docker` CLI and the `codeclash-runner:local` image (`docker compose --profile build build runner-image`).

## Judging

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant P as Problem Service
  participant K as Redpanda
  participant R as Runner Service
  participant S as Docker sandbox

  C->>P: POST /api/v1/submissions
  P->>P: insert submission (queued) + outbox record, one transaction
  P-->>K: problem.submission.cdc (WRITE, queued)
  K-->>R: consume
  R->>R: insert job into submissions job table, signal workers
  loop each worker (4 virtual threads)
    R->>R: claim oldest queued job (for update skip locked), lease 5 min, attempts + 1
    R->>S: take a warm container from the pool
    R->>S: write source, compile.sh
    alt compilation error
      R-->>K: runner.submission.state (compilation_error)
    else compiled
      R-->>K: runner.submission.state (compiled)
      loop each test case in creation order
        R->>S: run.sh with time limit x language multiplier
        S-->>R: OK / TLE / MLE / RE, time, memory
      end
      R->>R: compare output, stop at the first failure
      R-->>K: runner.submission.state (verdict)
    end
    R->>S: remove container
  end
  K-->>P: consume state
  P->>P: apply only if the current state is a valid predecessor
  P-->>K: problem.submission.cdc (WRITE, new state)
```
