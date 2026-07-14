package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class InvalidPasswordHashException extends CodedException {

  public InvalidPasswordHashException() {
    super(UserErrorCode.INVALID_PASSWORD_HASH, "Password hash cannot be blank");
  }
}
