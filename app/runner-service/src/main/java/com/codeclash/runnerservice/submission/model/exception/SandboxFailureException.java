package com.codeclash.runnerservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class SandboxFailureException extends CodedException {

  public SandboxFailureException(String message) {
    super(SubmissionErrorCode.SANDBOX_FAILURE, message);
  }
}
