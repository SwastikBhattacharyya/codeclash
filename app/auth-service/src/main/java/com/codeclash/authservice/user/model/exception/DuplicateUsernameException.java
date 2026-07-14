package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateUsernameException extends CodedException {

  private final String username;

  public DuplicateUsernameException(String username) {
    super(
        UserErrorCode.DUPLICATE_USERNAME,
        "User with username %s already exists".formatted(username));
    this.username = username;
  }
}
