package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class InvalidCredentialsException extends CodedException {

  public InvalidCredentialsException() {
    super(SessionErrorCode.INVALID_CREDENTIALS, "Email or password is incorrect");
  }
}
