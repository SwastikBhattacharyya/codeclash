package com.codeclash.problemservice.question.model;

import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooLowException;
import com.codeclash.problemservice.question.model.exception.NullMaxRamLimitException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class MaxRamLimit {

  public static final int MIN = 64;
  public static final int MAX = 1024;
  public static final int DEFAULT = 256;

  private final int value;

  private MaxRamLimit(int value) {
    this.value = value;
  }

  public static MaxRamLimit of(Integer value) {
    if (value == null) throw new NullMaxRamLimitException();
    if (value < MaxRamLimit.MIN) throw new MaxRamLimitTooLowException(value);
    if (value > MaxRamLimit.MAX) throw new MaxRamLimitTooHighException(value);
    return new MaxRamLimit(value);
  }

  public static MaxRamLimit unsafe(int value) {
    return new MaxRamLimit(value);
  }

  public int value() {
    return this.value;
  }
}
