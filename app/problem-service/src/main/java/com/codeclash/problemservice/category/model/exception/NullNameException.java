package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullNameException extends CodedException {

  public NullNameException() {
    super(CategoryErrorCode.NULL_NAME, "Name cannot be null");
  }
}
