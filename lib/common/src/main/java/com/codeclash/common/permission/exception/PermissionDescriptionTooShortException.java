package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.permission.Description;
import lombok.Getter;

@Getter
public class PermissionDescriptionTooShortException extends CodedException {

  private final String description;

  public PermissionDescriptionTooShortException(String description) {
    super(
        PermissionErrorCode.PERMISSION_DESCRIPTION_TOO_SHORT,
        "Permission description must contain at least %d characters"
            .formatted(Description.MIN_SIZE));
    this.description = description;
  }
}
