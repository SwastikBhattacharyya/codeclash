package com.codeclash.problemservice.submission.model;

import com.codeclash.problemservice.submission.model.exception.InvalidUserIdException;
import com.codeclash.problemservice.submission.model.exception.NullUserIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class UserId {

  public static final int VERSION = 7;

  private final UUID value;

  private UserId(UUID value) {
    this.value = value;
  }

  public static UserId of(UUID value) {
    if (value == null) throw new NullUserIdException();
    if (value.version() != UserId.VERSION) throw new InvalidUserIdException(value);
    return new UserId(value);
  }

  public static UserId unsafe(UUID value) {
    return new UserId(value);
  }

  public UUID value() {
    return this.value;
  }
}
