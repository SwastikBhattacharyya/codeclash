package com.codeclash.runnerservice.sandbox.api.exception;

import com.codeclash.common.exception.CodedException;

public class SandboxException extends CodedException {

  public static final int MAX_MESSAGE_SIZE = 512;

  public SandboxException(String message) {
    super(SandboxErrorCode.SANDBOX_FAILURE, SandboxException.truncate(message));
  }

  private static String truncate(String message) {
    if (message == null || message.length() <= SandboxException.MAX_MESSAGE_SIZE) return message;
    int end = SandboxException.MAX_MESSAGE_SIZE;
    if (Character.isHighSurrogate(message.charAt(end - 1))) end--;
    return message.substring(0, end);
  }
}
