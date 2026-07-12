package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateRoleNameException extends CodedException {

  private final String name;

  public DuplicateRoleNameException(String name) {
    super(RoleErrorCode.DUPLICATE_ROLE_NAME, "Role with name %s already exists".formatted(name));
    this.name = name;
  }
}
