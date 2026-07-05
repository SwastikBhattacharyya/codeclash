package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;

public class NullPermissionNameException extends CodedException {

  public NullPermissionNameException() {
    super(PermissionErrorCode.NULL_PERMISSION_NAME, "Permission name cannot be null");
  }
}
