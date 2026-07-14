package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class PasswordMissingUppercaseException extends CodedException {

  public PasswordMissingUppercaseException() {
    super(
        UserErrorCode.PASSWORD_MISSING_UPPERCASE,
        "Password must contain at least one uppercase letter");
  }
}
