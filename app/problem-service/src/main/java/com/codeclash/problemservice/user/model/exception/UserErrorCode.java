package com.codeclash.problemservice.user.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {
  DUPLICATE_USERNAME("Username already exists"),
  INVALID_USER_ID("Invalid user id"),
  NULL_USER_ID("User id is required"),
  NULL_USERNAME("Username is required");

  private final String title;

  UserErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
