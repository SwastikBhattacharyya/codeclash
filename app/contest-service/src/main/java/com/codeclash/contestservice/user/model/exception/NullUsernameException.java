package com.codeclash.contestservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullUsernameException extends CodedException {

  public NullUsernameException() {
    super(UserErrorCode.NULL_USERNAME, "Username cannot be null");
  }
}
