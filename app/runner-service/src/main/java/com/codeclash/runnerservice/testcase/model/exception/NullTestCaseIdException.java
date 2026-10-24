package com.codeclash.runnerservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullTestCaseIdException extends CodedException {

  public NullTestCaseIdException() {
    super(TestCaseErrorCode.NULL_TEST_CASE_ID, "Test case Id cannot be null");
  }
}
