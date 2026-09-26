package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullEndAtException extends CodedException {

  public NullEndAtException() {
    super(ContestErrorCode.NULL_END_AT, "End time cannot be null");
  }
}
