package com.codeclash.notificationservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullNameException extends CodedException {

  public NullNameException() {
    super(UserErrorCode.NULL_NAME, "Name cannot be null");
  }
}
