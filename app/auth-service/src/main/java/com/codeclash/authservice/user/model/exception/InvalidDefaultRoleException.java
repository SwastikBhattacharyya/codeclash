package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidDefaultRoleException extends CodedException {

  private final String name;

  public InvalidDefaultRoleException(String name) {
    super(
        UserErrorCode.INVALID_DEFAULT_ROLE,
        "Role %s cannot be the default user role because it is not a default role"
            .formatted(name));
    this.name = name;
  }
}
