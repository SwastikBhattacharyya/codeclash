package com.codeclash.runnerservice.submission.model;

import com.codeclash.contracts.problem.submission.SubmissionState;

public record SubmissionStateChange(
    Id id,
    SubmissionState state,
    TestCaseId failedTestCaseId,
    Long timeTakenMs,
    Long memoryUsedKb,
    String compileOutput,
    ActualOutput actualOutput) {

  public static SubmissionStateChange of(Id id, SubmissionState state) {
    return new SubmissionStateChange(id, state, null, null, null, null, null);
  }

  public static SubmissionStateChange compilationError(Id id, String compileOutput) {
    return new SubmissionStateChange(
        id, SubmissionState.COMPILATION_ERROR, null, null, null, compileOutput, null);
  }

  public static SubmissionStateChange failed(
      Id id, SubmissionState state, TestCaseId testCaseId, ActualOutput actualOutput) {
    return new SubmissionStateChange(id, state, testCaseId, null, null, null, actualOutput);
  }

  public static SubmissionStateChange correct(Id id, long timeTakenMs, long memoryUsedKb) {
    return new SubmissionStateChange(
        id, SubmissionState.CORRECT, null, timeTakenMs, memoryUsedKb, null, null);
  }
}
