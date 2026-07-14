package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Name;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class NameTooShortException extends CodedException {

  private final String name;

  public NameTooShortException(String name) {
    super(
        UserErrorCode.NAME_TOO_SHORT,
        "Name must contain at least %d characters".formatted(Name.MIN_SIZE));
    this.name = name;
  }
}
