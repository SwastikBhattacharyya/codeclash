package com.codeclash.runnerservice.question.model;

import com.codeclash.runnerservice.question.model.exception.InvalidMaxRunTimeException;
import com.codeclash.runnerservice.question.model.exception.NullMaxRunTimeException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class MaxRunTime {

  public static final int MIN = 100;
  public static final int MAX = 10000;

  private final int value;

  private MaxRunTime(int value) {
    this.value = value;
  }

  public static MaxRunTime of(Integer value) {
    if (value == null) throw new NullMaxRunTimeException();
    if (value < MaxRunTime.MIN || value > MaxRunTime.MAX) throw new InvalidMaxRunTimeException(value);
    return new MaxRunTime(value);
  }

  public static MaxRunTime unsafe(int value) {
    return new MaxRunTime(value);
  }

  public int value() {
    return this.value;
  }
}
