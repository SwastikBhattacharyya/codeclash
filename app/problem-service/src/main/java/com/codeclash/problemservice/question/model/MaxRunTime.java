package com.codeclash.problemservice.question.model;

import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooLowException;
import com.codeclash.problemservice.question.model.exception.NullMaxRunTimeException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class MaxRunTime {

  public static final int MIN = 100;
  public static final int MAX = 10000;
  public static final int DEFAULT = 2000;

  private final int value;

  private MaxRunTime(int value) {
    this.value = value;
  }

  public static MaxRunTime of(Integer value) {
    if (value == null) throw new NullMaxRunTimeException();
    if (value < MaxRunTime.MIN) throw new MaxRunTimeTooLowException(value);
    if (value > MaxRunTime.MAX) throw new MaxRunTimeTooHighException(value);
    return new MaxRunTime(value);
  }

  public static MaxRunTime unsafe(int value) {
    return new MaxRunTime(value);
  }

  public int value() {
    return this.value;
  }
}
