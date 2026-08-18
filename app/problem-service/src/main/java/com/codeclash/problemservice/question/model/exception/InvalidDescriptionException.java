package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidDescriptionException extends CodedException {

  private final String description;

  public InvalidDescriptionException(String description) {
    super(
        QuestionErrorCode.INVALID_DESCRIPTION, "Description must not have surrounding whitespace");
    this.description = description;
  }
}
