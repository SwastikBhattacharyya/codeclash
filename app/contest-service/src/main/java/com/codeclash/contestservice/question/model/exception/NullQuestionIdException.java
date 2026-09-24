package com.codeclash.contestservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullQuestionIdException extends CodedException {

  public NullQuestionIdException() {
    super(QuestionErrorCode.NULL_QUESTION_ID, "Question Id cannot be null");
  }
}
