package com.codeclash.notificationservice.user.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {
  INVALID_USER_ID("Invalid user id"),
  INVALID_VERIFICATION_TOKEN("Invalid verification token"),
  NULL_EMAIL("Email is required"),
  NULL_NAME("Name is required"),
  NULL_USER_ID("User id is required"),
  NULL_VERIFICATION_TOKEN("Verification token is required"),
  NULL_VERIFIED("Verified is required");

  private final String title;

  UserErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
