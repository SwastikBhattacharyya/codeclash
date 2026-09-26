package com.codeclash.contestservice.contest.model.registration;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class UserId {

  private final UUID value;

  private UserId(UUID value) {
    this.value = value;
  }

  public static UserId unsafe(UUID value) {
    return new UserId(value);
  }

  public UUID value() {
    return this.value;
  }
}
