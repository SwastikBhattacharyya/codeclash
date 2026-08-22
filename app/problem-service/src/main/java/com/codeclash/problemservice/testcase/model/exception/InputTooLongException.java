package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.testcase.model.Input;

public class InputTooLongException extends CodedException {

  public InputTooLongException() {
    super(
        TestCaseErrorCode.INPUT_TOO_LONG,
        "Input must contain at most %d characters".formatted(Input.MAX_SIZE));
  }
}
