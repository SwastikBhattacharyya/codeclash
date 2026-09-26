package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LiveAtInPastException extends CodedException {

  private final Instant liveAt;

  public LiveAtInPastException(Instant liveAt) {
    super(
        ContestErrorCode.LIVE_AT_IN_PAST,
        "Live time %s must be in the future".formatted(liveAt));
    this.liveAt = liveAt;
  }
}
