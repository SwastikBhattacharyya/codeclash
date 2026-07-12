package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullPermissionsException extends CodedException {

  public NullPermissionsException() {
    super(RoleErrorCode.NULL_PERMISSIONS, "Permissions cannot be null or contain null");
  }
}
