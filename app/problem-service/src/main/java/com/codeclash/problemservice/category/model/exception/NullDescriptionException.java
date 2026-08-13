package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullDescriptionException extends CodedException {

  public NullDescriptionException() {
    super(CategoryErrorCode.NULL_DESCRIPTION, "Description cannot be null");
  }
}
