package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class EmailTooLongException extends CodedException {

  private final String email;

  public EmailTooLongException(String email) {
    super(
        UserErrorCode.EMAIL_TOO_LONG,
        "Email must contain at most %d characters".formatted(Email.MAX_SIZE));
    this.email = email;
  }
}
