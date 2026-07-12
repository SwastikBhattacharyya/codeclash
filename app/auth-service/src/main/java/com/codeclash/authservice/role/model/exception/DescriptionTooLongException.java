package com.codeclash.authservice.role.model.exception;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DescriptionTooLongException extends CodedException {

  private final String description;

  public DescriptionTooLongException(String description) {
    super(
        RoleErrorCode.DESCRIPTION_TOO_LONG,
        "Description must contain at most %d characters".formatted(Description.MAX_SIZE));
    this.description = description;
  }
}
