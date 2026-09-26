package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullQuestionIdException extends CodedException {

  public NullQuestionIdException() {
    super(ContestErrorCode.NULL_QUESTION_ID, "Question id cannot be null");
  }
}
