package com.codeclash.runnerservice.submission.model;

import com.codeclash.contracts.problem.submission.Language;
import java.time.Duration;

public record Job(
    Id id,
    Language language,
    Code code,
    QuestionId questionId,
    JobStatus status,
    int attempts,
    TraceCarrier traceCarrier,
    Duration queuedFor) {}
