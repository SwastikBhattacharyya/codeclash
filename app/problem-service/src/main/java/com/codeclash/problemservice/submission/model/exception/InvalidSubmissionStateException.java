package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidSubmissionStateException extends CodedException {

  private final String value;

  public InvalidSubmissionStateException(String value) {
    super(
        SubmissionErrorCode.INVALID_SUBMISSION_STATE,
        "%s is not a valid submission state".formatted(value));
    this.value = value;
  }
}
