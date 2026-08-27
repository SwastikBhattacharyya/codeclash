package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullUserIdException extends CodedException {

  public NullUserIdException() {
    super(SubmissionErrorCode.NULL_USER_ID, "User Id cannot be null");
  }
}
