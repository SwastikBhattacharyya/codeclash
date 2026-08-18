package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Description;
import lombok.Getter;

@Getter
public class DescriptionTooLongException extends CodedException {

  private final String description;

  public DescriptionTooLongException(String description) {
    super(
        QuestionErrorCode.DESCRIPTION_TOO_LONG,
        "Description must contain at most %d characters".formatted(Description.MAX_SIZE));
    this.description = description;
  }
}
