package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullDescriptionException extends CodedException {

  public NullDescriptionException() {
    super(TagErrorCode.NULL_DESCRIPTION, "Description cannot be null");
  }
}
