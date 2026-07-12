package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullDescriptionException extends CodedException {

  public NullDescriptionException() {
    super(RoleErrorCode.NULL_DESCRIPTION, "Description cannot be null");
  }
}
