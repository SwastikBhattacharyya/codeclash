package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.permission.Name;
import lombok.Getter;

@Getter
public class PermissionNameTooLongException extends CodedException {

  private final String name;

  public PermissionNameTooLongException(String name) {
    super(
        PermissionErrorCode.PERMISSION_NAME_TOO_LONG,
        "Permission name must contain at most %d characters".formatted(Name.MAX_SIZE));
    this.name = name;
  }
}
