package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullDescriptionException extends CodedException {

  public NullDescriptionException() {
    super(ContestErrorCode.NULL_DESCRIPTION, "Description cannot be null");
  }
}
