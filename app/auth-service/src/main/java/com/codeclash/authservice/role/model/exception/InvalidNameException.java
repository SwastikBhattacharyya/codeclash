package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidNameException extends CodedException {

  private final String name;

  public InvalidNameException(String name) {
    super(
        RoleErrorCode.INVALID_NAME,
        "Name %s must be lowercase letters separated by single underscores".formatted(name));
    this.name = name;
  }
}
