package com.codeclash.contracts.auth.session.exception;

import com.codeclash.common.exception.CodedException;

public class AuthUnavailableException extends CodedException {

  public AuthUnavailableException() {
    super(AuthErrorCode.AUTH_UNAVAILABLE, "Authentication service is unavailable");
  }

  public AuthUnavailableException(Throwable cause) {
    this();
    this.initCause(cause);
  }
}
