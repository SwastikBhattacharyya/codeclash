package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidSubmissionResultException extends CodedException {

  private final String state;

  public InvalidSubmissionResultException(String state) {
    super(
        SubmissionErrorCode.INVALID_SUBMISSION_RESULT,
        "Result fields are inconsistent with state %s".formatted(state));
    this.state = state;
  }
}
