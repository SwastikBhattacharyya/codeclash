package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidTagIdException extends CodedException {

  private final UUID id;

  public InvalidTagIdException(UUID id) {
    super(QuestionErrorCode.INVALID_TAG_ID, "%s is not a valid tag id".formatted(id));
    this.id = id;
  }
}
