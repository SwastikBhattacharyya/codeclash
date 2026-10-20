package com.codeclash.contracts.runner.event;

import com.codeclash.contracts.problem.submission.SubmissionState;
import java.util.UUID;

public record SubmissionStateEventData(
    UUID submissionId,
    SubmissionState state,
    UUID failedTestCaseId,
    Long timeTakenMs,
    Long memoryUsedKb,
    String compileOutput,
    String actualOutput) {}
