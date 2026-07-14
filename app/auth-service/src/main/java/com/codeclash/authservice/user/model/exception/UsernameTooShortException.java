package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Username;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class UsernameTooShortException extends CodedException {

  private final String username;

  public UsernameTooShortException(String username) {
    super(
        UserErrorCode.USERNAME_TOO_SHORT,
        "Username must contain at least %d characters".formatted(Username.MIN_SIZE));
    this.username = username;
  }
}
