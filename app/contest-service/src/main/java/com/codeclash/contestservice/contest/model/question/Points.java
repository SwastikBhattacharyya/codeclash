package com.codeclash.contestservice.contest.model.question;

import com.codeclash.contestservice.contest.model.exception.NullPointsException;
import com.codeclash.contestservice.contest.model.exception.PointsTooHighException;
import com.codeclash.contestservice.contest.model.exception.PointsTooLowException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Points {

  public static final int MIN = 1;
  public static final int MAX = 10000;

  private final int value;

  private Points(int value) {
    this.value = value;
  }

  public static Points of(Integer value) {
    if (value == null) throw new NullPointsException();
    if (value < Points.MIN) throw new PointsTooLowException(value);
    if (value > Points.MAX) throw new PointsTooHighException(value);
    return new Points(value);
  }

  public static Points unsafe(int value) {
    return new Points(value);
  }

  public int value() {
    return this.value;
  }
}
