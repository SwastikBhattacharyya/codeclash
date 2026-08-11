package com.codeclash.problemservice.user.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullUserIdException extends CodedException {

  public NullUserIdException() {
    super(UserErrorCode.NULL_USER_ID, "User Id cannot be null");
  }
}
