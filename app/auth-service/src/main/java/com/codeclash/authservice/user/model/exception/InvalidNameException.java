package com.codeclash.authservice.user.model.exception;

import com.codeclash.authservice.user.model.Name;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidNameException extends CodedException {

  private final String name;

  public InvalidNameException(String name) {
    super(
        UserErrorCode.INVALID_NAME,
        "Name must contain at least %d alphabets".formatted(Name.MIN_LETTERS));
    this.name = name;
  }
}
