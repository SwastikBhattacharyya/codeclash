package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.contestservice.contest.model.Description;
import lombok.Getter;

@Getter
public class DescriptionTooShortException extends CodedException {

  private final String description;

  public DescriptionTooShortException(String description) {
    super(
        ContestErrorCode.DESCRIPTION_TOO_SHORT,
        "Description must contain at least %d characters".formatted(Description.MIN_SIZE));
    this.description = description;
  }
}
