package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullFailedTestCaseIdException extends CodedException {

  public NullFailedTestCaseIdException() {
    super(SubmissionErrorCode.NULL_FAILED_TEST_CASE_ID, "Failed test case Id cannot be null");
  }
}
