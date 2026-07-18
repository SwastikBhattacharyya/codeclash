package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;

public class AmbiguousCredentialsException extends CodedException {

  public AmbiguousCredentialsException() {
    super(SessionErrorCode.AMBIGUOUS_CREDENTIALS, "Send either email or username, not both");
  }
}
