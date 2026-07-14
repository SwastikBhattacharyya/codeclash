package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateEmailException extends CodedException {

  private final String email;

  public DuplicateEmailException(String email) {
    super(UserErrorCode.DUPLICATE_EMAIL, "User with email %s already exists".formatted(email));
    this.email = email;
  }
}
