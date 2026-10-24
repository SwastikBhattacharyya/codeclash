package com.codeclash.runnerservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullPublicException extends CodedException {

  public NullPublicException() {
    super(TestCaseErrorCode.NULL_PUBLIC, "Public cannot be null");
  }
}
