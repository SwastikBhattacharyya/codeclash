package com.codeclash.runnerservice.sandbox.api.exception;

import com.codeclash.common.exception.ErrorCode;

public enum SandboxErrorCode implements ErrorCode {
  SANDBOX_FAILURE("Sandbox failure");

  private final String title;

  SandboxErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
