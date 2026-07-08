package com.codeclash.contracts.auth.session.exception;

import com.codeclash.common.exception.CodedException;

public class UnauthenticatedException extends CodedException {

  public UnauthenticatedException() {
    super(AuthErrorCode.UNAUTHENTICATED, "Authentication is required");
  }
}
