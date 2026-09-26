package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.contestservice.contest.model.question.Points;
import lombok.Getter;

@Getter
public class PointsTooLowException extends CodedException {

  private final Integer points;

  public PointsTooLowException(Integer points) {
    super(
        ContestErrorCode.POINTS_TOO_LOW,
        "Points must be at least %d".formatted(Points.MIN));
    this.points = points;
  }
}
