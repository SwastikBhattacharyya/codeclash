package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.common.exception.CodedException;

public class PasswordTooShortException extends CodedException {

  public PasswordTooShortException() {
    super(
        UserErrorCode.PASSWORD_TOO_SHORT,
        "Password must contain at least %d characters".formatted(Password.MIN_SIZE));
  }
}
