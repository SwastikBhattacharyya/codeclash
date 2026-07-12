package com.codeclash.authservice.role.api.exception;

import com.codeclash.authservice.role.model.exception.RoleErrorCode;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class RoleNameNotFoundException extends CodedException {

  private final String name;

  public RoleNameNotFoundException(String name) {
    super(RoleErrorCode.ROLE_NOT_FOUND, "Role with name %s not found".formatted(name));
    this.name = name;
  }
}
