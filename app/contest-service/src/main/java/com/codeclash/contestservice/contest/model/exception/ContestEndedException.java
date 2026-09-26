package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ContestEndedException extends CodedException {

  private final UUID id;
  private final Instant endAt;

  public ContestEndedException(UUID id, Instant endAt) {
    super(ContestErrorCode.CONTEST_ENDED, "Contest %s ended at %s".formatted(id, endAt));
    this.id = id;
    this.endAt = endAt;
  }
}
