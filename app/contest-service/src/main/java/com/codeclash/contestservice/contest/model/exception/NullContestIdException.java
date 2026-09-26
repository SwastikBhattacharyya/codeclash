package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullContestIdException extends CodedException {

  public NullContestIdException() {
    super(ContestErrorCode.NULL_CONTEST_ID, "Contest id cannot be null");
  }
}
