package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullNameException extends CodedException {

  public NullNameException() {
    super(TagErrorCode.NULL_NAME, "Name cannot be null");
  }
}
