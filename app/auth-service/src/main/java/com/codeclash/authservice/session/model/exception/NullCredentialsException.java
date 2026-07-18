package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullCredentialsException extends CodedException {

  public NullCredentialsException() {
    super(SessionErrorCode.NULL_CREDENTIALS, "Email or username, and password, cannot be null");
  }
}
