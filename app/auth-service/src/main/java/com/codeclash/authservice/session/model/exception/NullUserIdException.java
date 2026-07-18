package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullUserIdException extends CodedException {

  public NullUserIdException() {
    super(SessionErrorCode.NULL_USER_ID, "User id cannot be null");
  }
}
