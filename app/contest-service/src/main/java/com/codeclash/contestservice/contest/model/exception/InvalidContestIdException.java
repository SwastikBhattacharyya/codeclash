package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidContestIdException extends CodedException {

  private final UUID id;

  public InvalidContestIdException(UUID id) {
    super(
        ContestErrorCode.INVALID_CONTEST_ID,
        "%s is not a valid contest id".formatted(id));
    this.id = id;
  }
}
