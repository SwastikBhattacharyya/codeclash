package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidQuestionIdException extends CodedException {

  private final UUID id;

  public InvalidQuestionIdException(UUID id) {
    super(
        ContestErrorCode.INVALID_QUESTION_ID,
        "%s is not a valid question id".formatted(id));
    this.id = id;
  }
}
