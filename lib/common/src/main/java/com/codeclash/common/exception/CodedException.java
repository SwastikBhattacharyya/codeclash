package com.codeclash.common.exception;

import lombok.Getter;

@Getter
public abstract class CodedException extends RuntimeException {

  private final ErrorCode errorCode;

  protected CodedException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
