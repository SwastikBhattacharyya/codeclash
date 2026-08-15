package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateTagNameException extends CodedException {

  private final String name;

  public DuplicateTagNameException(String name) {
    super(TagErrorCode.DUPLICATE_TAG_NAME, "Tag with name %s already exists".formatted(name));
    this.name = name;
  }
}
