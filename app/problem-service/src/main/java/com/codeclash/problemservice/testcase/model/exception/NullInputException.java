package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullInputException extends CodedException {

  public NullInputException() {
    super(TestCaseErrorCode.NULL_INPUT, "Input cannot be null");
  }
}
