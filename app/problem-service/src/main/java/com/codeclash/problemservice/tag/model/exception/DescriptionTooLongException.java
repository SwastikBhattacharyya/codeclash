package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.tag.model.Description;
import lombok.Getter;

@Getter
public class DescriptionTooLongException extends CodedException {

  private final String description;

  public DescriptionTooLongException(String description) {
    super(
        TagErrorCode.DESCRIPTION_TOO_LONG,
        "Description must contain at most %d characters".formatted(Description.MAX_SIZE));
    this.description = description;
  }
}
