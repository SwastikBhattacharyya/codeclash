package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullLastUsedAtException extends CodedException {

  public NullLastUsedAtException() {
    super(SessionErrorCode.NULL_LAST_USED_AT, "Last used time cannot be null");
  }
}
