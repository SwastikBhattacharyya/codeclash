package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullSessionIdException extends CodedException {

  public NullSessionIdException() {
    super(SessionErrorCode.NULL_SESSION_ID, "Session id cannot be null");
  }
}
