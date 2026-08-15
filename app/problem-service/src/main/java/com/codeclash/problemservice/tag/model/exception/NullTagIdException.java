package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullTagIdException extends CodedException {

  public NullTagIdException() {
    super(TagErrorCode.NULL_TAG_ID, "Tag Id cannot be null");
  }
}
