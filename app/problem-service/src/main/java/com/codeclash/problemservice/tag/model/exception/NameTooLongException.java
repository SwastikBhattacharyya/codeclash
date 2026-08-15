package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.tag.model.Name;
import lombok.Getter;

@Getter
public class NameTooLongException extends CodedException {

  private final String name;

  public NameTooLongException(String name) {
    super(
        TagErrorCode.NAME_TOO_LONG,
        "Name must contain at most %d characters".formatted(Name.MAX_SIZE));
    this.name = name;
  }
}
