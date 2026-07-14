package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Username;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidUsernameException extends CodedException {

  private final String username;

  public InvalidUsernameException(String username) {
    super(
        UserErrorCode.INVALID_USERNAME,
        "Username may only contain the characters %s".formatted(Username.ALLOWED_CHARACTERS));
    this.username = username;
  }
}
