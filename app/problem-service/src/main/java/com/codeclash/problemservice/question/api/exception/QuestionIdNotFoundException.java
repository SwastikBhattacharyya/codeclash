package com.codeclash.problemservice.question.api.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.exception.QuestionErrorCode;
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
