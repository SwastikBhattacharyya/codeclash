package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class PasswordMissingSpecialCharacterException extends CodedException {

  public PasswordMissingSpecialCharacterException() {
    super(
        UserErrorCode.PASSWORD_MISSING_SPECIAL_CHARACTER,
        "Password must contain at least one special character");
  }
}
