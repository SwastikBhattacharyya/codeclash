package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ContestNotFoundException extends CodedException {

  private final UUID id;

  public ContestNotFoundException(UUID id) {
    super(
        ContestErrorCode.CONTEST_NOT_FOUND,
        "Contest with id %s not found".formatted(id));
    this.id = id;
  }
}
