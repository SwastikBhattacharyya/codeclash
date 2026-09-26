package com.codeclash.contestservice.contest.model;

import com.codeclash.contestservice.contest.model.exception.NullEndAtException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record EndAt(Instant value) {

  public EndAt {
    if (value == null) throw new NullEndAtException();
    value = value.truncatedTo(ChronoUnit.MICROS);
  }
}
