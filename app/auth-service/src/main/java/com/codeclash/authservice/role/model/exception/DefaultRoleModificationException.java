package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DefaultRoleModificationException extends CodedException {

  private final UUID id;

  public DefaultRoleModificationException(UUID id) {
    super(
        RoleErrorCode.DEFAULT_ROLE_MODIFICATION,
        "Role with id %s is a default role and cannot be modified".formatted(id));
    this.id = id;
  }
}
