package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Description;
import lombok.Getter;

@Getter
public class DescriptionTooShortException extends CodedException {

  private final String description;

  public DescriptionTooShortException(String description) {
    super(
        QuestionErrorCode.DESCRIPTION_TOO_SHORT,
        "Description must contain at least %d characters".formatted(Description.MIN_SIZE));
    this.description = description;
  }
}
