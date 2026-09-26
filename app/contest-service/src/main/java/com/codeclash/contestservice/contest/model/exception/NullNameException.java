package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullNameException extends CodedException {

  public NullNameException() {
    super(ContestErrorCode.NULL_NAME, "Name cannot be null");
  }
}
