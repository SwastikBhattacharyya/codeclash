package com.codeclash.problemservice.contest.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum ContestErrorCode implements ErrorCode {
  INVALID_CONTEST_ID("Invalid contest id"),
  NULL_CONTEST_ID("Contest id is required");

  private final String title;

  ContestErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
