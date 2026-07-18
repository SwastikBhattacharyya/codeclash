package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class UnverifiedUserException extends CodedException {

  public UnverifiedUserException() {
    super(SessionErrorCode.UNVERIFIED_USER, "User is not verified");
  }
}
