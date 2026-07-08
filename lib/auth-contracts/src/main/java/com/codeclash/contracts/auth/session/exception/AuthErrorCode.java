package com.codeclash.contracts.auth.session.exception;

import com.codeclash.common.exception.ErrorCode;

public enum AuthErrorCode implements ErrorCode {
  AUTH_UNAVAILABLE("Authentication unavailable"),
  FORBIDDEN("Forbidden"),
  UNAUTHENTICATED("Unauthenticated");

  private final String title;

  AuthErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
