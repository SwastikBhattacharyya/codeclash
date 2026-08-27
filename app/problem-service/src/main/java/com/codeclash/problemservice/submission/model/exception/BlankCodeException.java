package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class BlankCodeException extends CodedException {

  public BlankCodeException() {
    super(SubmissionErrorCode.BLANK_CODE, "Code must contain a non-whitespace character");
  }
}
