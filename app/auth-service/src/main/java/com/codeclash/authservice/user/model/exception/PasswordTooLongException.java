package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.common.exception.CodedException;

public class PasswordTooLongException extends CodedException {

  public PasswordTooLongException() {
    super(
        UserErrorCode.PASSWORD_TOO_LONG,
        "Password must contain at most %d characters".formatted(Password.MAX_SIZE));
  }
}
