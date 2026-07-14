package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidRoleIdException extends CodedException {

  private final UUID id;

  public InvalidRoleIdException(UUID id) {
    super(UserErrorCode.INVALID_ROLE_ID, "%s is not a valid role id".formatted(id));
    this.id = id;
  }
}
