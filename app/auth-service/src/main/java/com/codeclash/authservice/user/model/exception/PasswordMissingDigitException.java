package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class PasswordMissingDigitException extends CodedException {

  public PasswordMissingDigitException() {
    super(UserErrorCode.PASSWORD_MISSING_DIGIT, "Password must contain at least one number");
  }
}
