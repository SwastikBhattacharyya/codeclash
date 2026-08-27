package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullActualOutputException extends CodedException {

  public NullActualOutputException() {
    super(SubmissionErrorCode.NULL_ACTUAL_OUTPUT, "Actual output cannot be null");
  }
}
