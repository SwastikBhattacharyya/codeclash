package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class VerificationTokenMismatchException extends CodedException {

  public VerificationTokenMismatchException() {
    super(UserErrorCode.VERIFICATION_TOKEN_MISMATCH, "Verification token does not match");
  }
}
