package com.codeclash.problemservice.submission.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import lombok.With;

@With
public record Submission(
    Id id,
    Language language,
    Code code,
    SubmissionState state,
    FailedTestCaseId failedTestCaseId,
    TimeTaken timeTaken,
    MemoryUsed memoryUsed,
    CompileOutput compileOutput,
    ActualOutput actualOutput,
    QuestionId questionId,
    UserId userId,
    Username username,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}
