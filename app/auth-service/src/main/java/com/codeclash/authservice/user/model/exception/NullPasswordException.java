package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullPasswordException extends CodedException {

  public NullPasswordException() {
    super(UserErrorCode.NULL_PASSWORD, "Password cannot be null");
  }
}
