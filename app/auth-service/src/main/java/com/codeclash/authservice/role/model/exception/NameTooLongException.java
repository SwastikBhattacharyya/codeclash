package com.codeclash.authservice.role.model.exception;

import com.codeclash.authservice.role.model.Name;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class NameTooLongException extends CodedException {

  private final String name;

  public NameTooLongException(String name) {
    super(
        RoleErrorCode.NAME_TOO_LONG,
        "Name must contain at most %d characters".formatted(Name.MAX_SIZE));
    this.name = name;
  }
}
