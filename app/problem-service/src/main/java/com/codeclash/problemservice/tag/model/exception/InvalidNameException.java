package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidNameException extends CodedException {

  private final String name;

  public InvalidNameException(String name) {
    super(TagErrorCode.INVALID_NAME, "Name must be a single line without surrounding whitespace");
    this.name = name;
  }
}
