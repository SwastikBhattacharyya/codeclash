package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidDescriptionException extends CodedException {

  private final String description;

  public InvalidDescriptionException(String description) {
    super(
        CategoryErrorCode.INVALID_DESCRIPTION, "Description must not have surrounding whitespace");
    this.description = description;
  }
}
