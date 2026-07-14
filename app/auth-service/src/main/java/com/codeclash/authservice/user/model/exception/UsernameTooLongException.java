package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Username;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class UsernameTooLongException extends CodedException {

  private final String username;

  public UsernameTooLongException(String username) {
    super(
        UserErrorCode.USERNAME_TOO_LONG,
        "Username must contain at most %d characters".formatted(Username.MAX_SIZE));
    this.username = username;
  }
}
