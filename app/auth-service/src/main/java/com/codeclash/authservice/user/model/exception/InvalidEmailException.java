package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidEmailException extends CodedException {

  private final String email;

  public InvalidEmailException(String email) {
    super(UserErrorCode.INVALID_EMAIL, "Email is not valid");
    this.email = email;
  }
}
