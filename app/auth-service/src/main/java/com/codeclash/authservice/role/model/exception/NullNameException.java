package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullNameException extends CodedException {

  public NullNameException() {
    super(RoleErrorCode.NULL_NAME, "Name cannot be null");
  }
}
