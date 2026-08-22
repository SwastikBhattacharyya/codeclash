package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.testcase.model.Output;

public class OutputTooLongException extends CodedException {

  public OutputTooLongException() {
    super(
        TestCaseErrorCode.OUTPUT_TOO_LONG,
        "Output must contain at most %d characters".formatted(Output.MAX_SIZE));
  }
}
