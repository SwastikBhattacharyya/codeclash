package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class EndAtNotAfterLiveAtException extends CodedException {

  private final Instant liveAt;
  private final Instant endAt;

  public EndAtNotAfterLiveAtException(Instant liveAt, Instant endAt) {
    super(ContestErrorCode.END_AT_NOT_AFTER_LIVE_AT, "End time must be after live time");
    this.liveAt = liveAt;
    this.endAt = endAt;
  }
}
