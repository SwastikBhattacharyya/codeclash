package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RoleNotFoundException extends CodedException {

  private final UUID id;

  public RoleNotFoundException(UUID id) {
    super(UserErrorCode.ROLE_NOT_FOUND, "Role with id %s not found".formatted(id));
    this.id = id;
  }
}
