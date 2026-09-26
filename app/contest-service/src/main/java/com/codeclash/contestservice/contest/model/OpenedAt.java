package com.codeclash.contestservice.contest.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record OpenedAt(Instant value) {

  public OpenedAt {
    value = value.truncatedTo(ChronoUnit.MICROS);
  }
}
