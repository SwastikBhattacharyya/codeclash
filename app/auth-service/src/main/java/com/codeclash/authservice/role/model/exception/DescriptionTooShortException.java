package com.codeclash.authservice.role.model.exception;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DescriptionTooShortException extends CodedException {

  private final String description;

  public DescriptionTooShortException(String description) {
    super(
        RoleErrorCode.DESCRIPTION_TOO_SHORT,
        "Description must contain at least %d characters".formatted(Description.MIN_SIZE));
    this.description = description;
  }
}
