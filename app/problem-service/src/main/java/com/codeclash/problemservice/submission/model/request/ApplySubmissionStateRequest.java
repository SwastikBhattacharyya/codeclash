package com.codeclash.problemservice.submission.model.request;

import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.ActualOutput;
import com.codeclash.problemservice.submission.model.CompileOutput;
import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.MemoryUsed;
import com.codeclash.problemservice.submission.model.TimeTaken;

public record ApplySubmissionStateRequest(
    Id id,
    SubmissionState state,
    FailedTestCaseId failedTestCaseId,
    TimeTaken timeTaken,
    MemoryUsed memoryUsed,
    CompileOutput compileOutput,
    ActualOutput actualOutput) {}
