package com.codeclash.runnerservice.question.model;

import com.codeclash.runnerservice.question.model.exception.InvalidMaxRamLimitException;
import com.codeclash.runnerservice.question.model.exception.NullMaxRamLimitException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class MaxRamLimit {

  public static final int MIN = 64;
  public static final int MAX = 1024;

  private final int value;

  private MaxRamLimit(int value) {
    this.value = value;
  }

  public static MaxRamLimit of(Integer value) {
    if (value == null) throw new NullMaxRamLimitException();
    if (value < MaxRamLimit.MIN || value > MaxRamLimit.MAX) throw new InvalidMaxRamLimitException(value);
    return new MaxRamLimit(value);
  }

  public static MaxRamLimit unsafe(int value) {
    return new MaxRamLimit(value);
  }

  public int value() {
    return this.value;
  }
}
