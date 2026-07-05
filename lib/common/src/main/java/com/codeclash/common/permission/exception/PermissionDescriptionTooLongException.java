package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.permission.Description;
import lombok.Getter;

@Getter
public class PermissionDescriptionTooLongException extends CodedException {

  private final String description;

  public PermissionDescriptionTooLongException(String description) {
    super(
        PermissionErrorCode.PERMISSION_DESCRIPTION_TOO_LONG,
        "Permission description must contain at most %d characters"
            .formatted(Description.MAX_SIZE));
    this.description = description;
  }
}
