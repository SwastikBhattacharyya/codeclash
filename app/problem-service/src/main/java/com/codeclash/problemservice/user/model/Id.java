package com.codeclash.problemservice.user.model;

import com.codeclash.problemservice.user.model.exception.InvalidUserIdException;
import com.codeclash.problemservice.user.model.exception.NullUserIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Id {

  public static final int VERSION = 7;

  private final UUID value;

  private Id(UUID value) {
    this.value = value;
  }

  public static Id of(UUID value) {
    if (value == null) throw new NullUserIdException();
    if (value.version() != Id.VERSION) throw new InvalidUserIdException(value);
    return new Id(value);
  }

  public static Id unsafe(UUID value) {
    return new Id(value);
  }

  public UUID value() {
    return this.value;
  }
}
