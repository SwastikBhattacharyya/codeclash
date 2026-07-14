package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullVerificationTokenException extends CodedException {

  public NullVerificationTokenException() {
    super(UserErrorCode.NULL_VERIFICATION_TOKEN, "Verification token cannot be null");
  }
}
