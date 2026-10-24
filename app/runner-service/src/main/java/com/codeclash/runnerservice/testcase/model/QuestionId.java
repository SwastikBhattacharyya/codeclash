package com.codeclash.runnerservice.testcase.model;

import com.codeclash.runnerservice.testcase.model.exception.InvalidQuestionIdException;
import com.codeclash.runnerservice.testcase.model.exception.NullQuestionIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class QuestionId {

  public static final int VERSION = 7;

  private final UUID value;

  private QuestionId(UUID value) {
    this.value = value;
  }

  public static QuestionId of(UUID value) {
    if (value == null) throw new NullQuestionIdException();
    if (value.version() != QuestionId.VERSION) throw new InvalidQuestionIdException(value);
    return new QuestionId(value);
  }

  public static QuestionId unsafe(UUID value) {
    return new QuestionId(value);
  }

  public UUID value() {
    return this.value;
  }
}
