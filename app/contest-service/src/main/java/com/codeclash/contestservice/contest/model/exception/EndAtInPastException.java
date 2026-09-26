package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class EndAtInPastException extends CodedException {

  private final Instant endAt;

  public EndAtInPastException(Instant endAt) {
    super(
        ContestErrorCode.END_AT_IN_PAST,
        "End time %s must be in the future".formatted(endAt));
    this.endAt = endAt;
  }
}
