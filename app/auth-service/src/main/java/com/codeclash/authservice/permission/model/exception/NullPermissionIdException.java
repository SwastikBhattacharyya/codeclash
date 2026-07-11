package com.codeclash.authservice.permission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullPermissionIdException extends CodedException {

  public NullPermissionIdException() {
    super(PermissionErrorCode.NULL_PERMISSION_ID, "Permission id cannot be null");
  }
}
