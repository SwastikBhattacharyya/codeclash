package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullRoleIdException extends CodedException {

  public NullRoleIdException() {
    super(RoleErrorCode.NULL_ROLE_ID, "Role Id cannot be null");
  }
}
