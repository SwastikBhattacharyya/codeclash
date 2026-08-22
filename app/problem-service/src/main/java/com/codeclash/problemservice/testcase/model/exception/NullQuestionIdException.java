package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullQuestionIdException extends CodedException {

  public NullQuestionIdException() {
    super(TestCaseErrorCode.NULL_QUESTION_ID, "Question Id cannot be null");
  }
}
