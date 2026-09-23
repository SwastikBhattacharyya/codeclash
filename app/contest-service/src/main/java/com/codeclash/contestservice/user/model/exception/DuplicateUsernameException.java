package com.codeclash.contestservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateUsernameException extends CodedException {

  private final String username;

  public DuplicateUsernameException(String username) {
    super(
        UserErrorCode.DUPLICATE_USERNAME,
        "Username %s is still held by another user".formatted(username));
    this.username = username;
  }
}
