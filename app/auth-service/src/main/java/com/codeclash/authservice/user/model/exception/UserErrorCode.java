package com.codeclash.authservice.user.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {
  DUPLICATE_EMAIL("Email already exists"),
  DUPLICATE_USERNAME("Username already exists"),
  EMAIL_TOO_LONG("Email too long"),
  INVALID_DEFAULT_ROLE("Invalid default role"),
  INVALID_EMAIL("Invalid email"),
  INVALID_NAME("Invalid name"),
  INVALID_PASSWORD_HASH("Invalid password hash"),
  INVALID_ROLE_ID("Invalid role id"),
  INVALID_USER_ID("Invalid user id"),
  INVALID_USERNAME("Invalid username"),
  INVALID_VERIFICATION_TOKEN("Invalid verification token"),
  NAME_TOO_LONG("Name too long"),
  NAME_TOO_SHORT("Name too short"),
  NULL_EMAIL("Email is required"),
  NULL_NAME("Name is required"),
  NULL_PASSWORD("Password is required"),
  NULL_ROLE("Role is required"),
  NULL_USER_ID("User id is required"),
  NULL_USERNAME("Username is required"),
  NULL_VERIFICATION_TOKEN("Verification token is required"),
  NULL_VERIFIED("Verified is required"),
  PASSWORD_MISSING_DIGIT("Password missing digit"),
  PASSWORD_MISSING_LOWERCASE("Password missing lowercase letter"),
  PASSWORD_MISSING_SPECIAL_CHARACTER("Password missing special character"),
  PASSWORD_MISSING_UPPERCASE("Password missing uppercase letter"),
  PASSWORD_TOO_LONG("Password too long"),
  PASSWORD_TOO_SHORT("Password too short"),
  ROLE_NOT_FOUND("Role not found"),
  USER_NOT_FOUND("User not found"),
  USERNAME_TOO_LONG("Username too long"),
  USERNAME_TOO_SHORT("Username too short"),
  VERIFICATION_TOKEN_MISMATCH("Verification token mismatch");

  private final String title;

  UserErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
