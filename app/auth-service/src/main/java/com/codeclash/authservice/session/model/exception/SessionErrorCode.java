package com.codeclash.authservice.session.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum SessionErrorCode implements ErrorCode {
  AMBIGUOUS_CREDENTIALS("Ambiguous credentials"),
  INVALID_CREDENTIALS("Invalid credentials"),
  INVALID_SESSION_ID("Invalid session id"),
  INVALID_USER_ID("Invalid user id"),
  NULL_CREDENTIALS("Credentials are required"),
  NULL_LAST_USED_AT("Last used time is required"),
  NULL_SESSION_ID("Session id is required"),
  NULL_USER_ID("User id is required"),
  SESSION_NOT_FOUND("Session not found"),
  UNVERIFIED_USER("User is not verified"),
  USER_NOT_FOUND("User not found");

  private final String title;

  SessionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
