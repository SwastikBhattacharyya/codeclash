package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserNotFoundException extends CodedException {

  private final UUID userId;

  public UserNotFoundException(UUID userId) {
    super(SessionErrorCode.USER_NOT_FOUND, "User with id %s not found".formatted(userId));
    this.userId = userId;
  }
}
