package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidRoleIdException extends CodedException {

  private final UUID id;

  public InvalidRoleIdException(UUID id) {
    super(RoleErrorCode.INVALID_ROLE_ID, "%s is not a valid role id".formatted(id));
    this.id = id;
  }
}
