package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullLiveAtException extends CodedException {

  public NullLiveAtException() {
    super(ContestErrorCode.NULL_LIVE_AT, "Live time cannot be null");
  }
}
