package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullTagsException extends CodedException {

  public NullTagsException() {
    super(QuestionErrorCode.NULL_TAGS, "Tags cannot be null or contain null");
  }
}
