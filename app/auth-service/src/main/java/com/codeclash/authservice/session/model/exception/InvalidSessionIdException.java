package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidSessionIdException extends CodedException {

  private final UUID id;

  public InvalidSessionIdException(UUID id) {
    super(SessionErrorCode.INVALID_SESSION_ID, "%s is not a valid session id".formatted(id));
    this.id = id;
  }
}
