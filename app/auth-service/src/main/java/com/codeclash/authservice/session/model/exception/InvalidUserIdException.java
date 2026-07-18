package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidUserIdException extends CodedException {

  private final UUID userId;

  public InvalidUserIdException(UUID userId) {
    super(SessionErrorCode.INVALID_USER_ID, "%s is not a valid user id".formatted(userId));
    this.userId = userId;
  }
}
