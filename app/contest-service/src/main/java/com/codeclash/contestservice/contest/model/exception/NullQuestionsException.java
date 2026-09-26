package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullQuestionsException extends CodedException {

  public NullQuestionsException() {
    super(ContestErrorCode.NULL_QUESTIONS, "Questions cannot be null or contain null");
  }
}
