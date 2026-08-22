package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullOutputException extends CodedException {

  public NullOutputException() {
    super(TestCaseErrorCode.NULL_OUTPUT, "Output cannot be null");
  }
}
