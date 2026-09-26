package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateContestNameException extends CodedException {

  private final String name;

  public DuplicateContestNameException(String name) {
    super(
        ContestErrorCode.DUPLICATE_CONTEST_NAME,
        "Contest with name %s already exists".formatted(name));
    this.name = name;
  }
}
