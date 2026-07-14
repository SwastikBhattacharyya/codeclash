package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullRoleException extends CodedException {

  public NullRoleException() {
    super(UserErrorCode.NULL_ROLE, "Role cannot be null");
  }
}
