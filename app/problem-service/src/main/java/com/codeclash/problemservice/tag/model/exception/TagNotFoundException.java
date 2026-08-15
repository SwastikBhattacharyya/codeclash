package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TagNotFoundException extends CodedException {

  private final UUID id;

  public TagNotFoundException(UUID id) {
    super(TagErrorCode.TAG_NOT_FOUND, "Tag with id %s not found".formatted(id));
    this.id = id;
  }
}
