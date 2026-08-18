package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullDescriptionException extends CodedException {

  public NullDescriptionException() {
    super(QuestionErrorCode.NULL_DESCRIPTION, "Description cannot be null");
  }
}
