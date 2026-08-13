package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.category.model.Name;
import lombok.Getter;

@Getter
public class NameTooShortException extends CodedException {

  private final String name;

  public NameTooShortException(String name) {
    super(
        CategoryErrorCode.NAME_TOO_SHORT,
        "Name must contain at least %d characters".formatted(Name.MIN_SIZE));
    this.name = name;
  }
}
