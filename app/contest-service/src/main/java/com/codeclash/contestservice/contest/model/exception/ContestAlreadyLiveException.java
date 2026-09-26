package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ContestAlreadyLiveException extends CodedException {

  private final UUID id;
  private final Instant liveAt;

  public ContestAlreadyLiveException(UUID id, Instant liveAt) {
    super(
        ContestErrorCode.CONTEST_ALREADY_LIVE,
        "Contest %s went live at %s and is locked".formatted(id, liveAt));
    this.id = id;
    this.liveAt = liveAt;
  }
}
