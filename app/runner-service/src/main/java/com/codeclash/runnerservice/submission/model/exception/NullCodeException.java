package com.codeclash.runnerservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullCodeException extends CodedException {

  public NullCodeException() {
    super(SubmissionErrorCode.NULL_CODE, "Code cannot be null");
  }
}
