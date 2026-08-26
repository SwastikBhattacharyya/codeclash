package com.codeclash.contracts.problem.event;

import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import java.time.Instant;
import java.util.UUID;

public record SubmissionCdcEventData(
    UUID id,
    Language language,
    String code,
    SubmissionState state,
    UUID failedTestCaseId,
    Long timeTakenMs,
    Long memoryUsedKb,
    String compileOutput,
    String actualOutput,
    UUID questionId,
    UUID userId,
    Instant createdAt,
    Instant updatedAt) {}
