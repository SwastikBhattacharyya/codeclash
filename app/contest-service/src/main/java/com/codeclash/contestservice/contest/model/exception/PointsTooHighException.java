package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.contestservice.contest.model.question.Points;
import lombok.Getter;

@Getter
public class PointsTooHighException extends CodedException {

  private final Integer points;

  public PointsTooHighException(Integer points) {
    super(
        ContestErrorCode.POINTS_TOO_HIGH,
        "Points must be at most %d".formatted(Points.MAX));
    this.points = points;
  }
}
