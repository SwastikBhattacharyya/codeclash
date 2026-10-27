package com.codeclash.runnerservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullQuestionIdException extends CodedException {

  public NullQuestionIdException() {
    super(SubmissionErrorCode.NULL_QUESTION_ID, "Question Id cannot be null");
  }
}
