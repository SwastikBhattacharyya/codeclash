package com.codeclash.notificationservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullVerifiedException extends CodedException {

  public NullVerifiedException() {
    super(UserErrorCode.NULL_VERIFIED, "Verified cannot be null");
  }
}
