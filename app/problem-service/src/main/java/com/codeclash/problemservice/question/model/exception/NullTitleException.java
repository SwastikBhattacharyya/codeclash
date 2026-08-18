package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullTitleException extends CodedException {

  public NullTitleException() {
    super(QuestionErrorCode.NULL_TITLE, "Title cannot be null");
  }
}
