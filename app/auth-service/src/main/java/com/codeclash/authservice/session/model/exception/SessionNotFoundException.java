package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SessionNotFoundException extends CodedException {

  private final UUID id;

  public SessionNotFoundException(UUID id) {
    super(SessionErrorCode.SESSION_NOT_FOUND, "Session with id %s not found".formatted(id));
    this.id = id;
  }
}
