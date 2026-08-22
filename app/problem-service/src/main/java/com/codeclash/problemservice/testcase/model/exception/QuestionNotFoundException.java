package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class QuestionNotFoundException extends CodedException {

  private final UUID id;

  public QuestionNotFoundException(UUID id) {
    super(TestCaseErrorCode.QUESTION_NOT_FOUND, "Question with id %s not found".formatted(id));
    this.id = id;
  }
}
