package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class PasswordMissingLowercaseException extends CodedException {

  public PasswordMissingLowercaseException() {
    super(
        UserErrorCode.PASSWORD_MISSING_LOWERCASE,
        "Password must contain at least one lowercase letter");
  }
}
