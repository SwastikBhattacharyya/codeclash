package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullVisibleException extends CodedException {

  public NullVisibleException() {
    super(QuestionErrorCode.NULL_VISIBLE, "Visible cannot be null");
  }
}
