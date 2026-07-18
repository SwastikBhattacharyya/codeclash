package com.codeclash.authservice.session.model;

import com.codeclash.authservice.session.model.exception.NullLastUsedAtException;
import java.time.Instant;

public record LastUsedAt(Instant value) {

  public LastUsedAt {
    if (value == null) throw new NullLastUsedAtException();
  }
}
