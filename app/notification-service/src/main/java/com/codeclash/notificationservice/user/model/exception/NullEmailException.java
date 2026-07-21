package com.codeclash.notificationservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullEmailException extends CodedException {

  public NullEmailException() {
    super(UserErrorCode.NULL_EMAIL, "Email cannot be null");
  }
}
