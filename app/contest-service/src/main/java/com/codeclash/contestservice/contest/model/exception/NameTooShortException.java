package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.contestservice.contest.model.Name;
import lombok.Getter;

@Getter
public class NameTooShortException extends CodedException {

  private final String name;

  public NameTooShortException(String name) {
    super(
        ContestErrorCode.NAME_TOO_SHORT,
        "Name must contain at least %d characters".formatted(Name.MIN_SIZE));
    this.name = name;
  }
}
