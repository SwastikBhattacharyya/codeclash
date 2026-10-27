package com.codeclash.runnerservice.submission.model;

import com.codeclash.contracts.problem.submission.SubmissionState;

public record JudgeOutcome(boolean released, SubmissionState state) {

  public static JudgeOutcome retry() {
    return new JudgeOutcome(true, null);
  }

  public static JudgeOutcome finished(SubmissionState state) {
    return new JudgeOutcome(false, state);
  }
}
