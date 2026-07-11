package com.codeclash.authservice.permission.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicatePermissionNameException extends CodedException {

  private final String name;

  public DuplicatePermissionNameException(String name) {
    super(
        PermissionErrorCode.DUPLICATE_PERMISSION_NAME,
        "Permission name %s is defined more than once".formatted(name));
    this.name = name;
  }
}
