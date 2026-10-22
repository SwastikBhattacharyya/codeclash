package com.codeclash.runnerservice.question.api.exception;

import com.codeclash.runnerservice.question.model.exception.QuestionErrorCode;
import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class QuestionIdNotFoundException extends CodedException {

  private final UUID id;

  public QuestionIdNotFoundException(UUID id) {
    super(QuestionErrorCode.QUESTION_NOT_FOUND, "Question with id %s not found".formatted(id));
    this.id = id;
  }
}
