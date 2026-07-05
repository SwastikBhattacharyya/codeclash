package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;

public class NullPermissionDescriptionException extends CodedException {

  public NullPermissionDescriptionException() {
    super(PermissionErrorCode.NULL_PERMISSION_DESCRIPTION, "Permission description cannot be null");
  }
}
