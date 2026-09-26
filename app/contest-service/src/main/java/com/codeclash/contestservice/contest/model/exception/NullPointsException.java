package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullPointsException extends CodedException {

  public NullPointsException() {
    super(ContestErrorCode.NULL_POINTS, "Points cannot be null");
  }
}
