package com.codeclash.contestservice.contest.model;

import com.codeclash.contestservice.contest.model.exception.NullLiveAtException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record LiveAt(Instant value) {

  public LiveAt {
    if (value == null) throw new NullLiveAtException();
    value = value.truncatedTo(ChronoUnit.MICROS);
  }
}
