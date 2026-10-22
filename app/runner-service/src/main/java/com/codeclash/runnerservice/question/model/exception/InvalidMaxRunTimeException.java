package com.codeclash.runnerservice.question.model.exception;

import com.codeclash.runnerservice.question.model.MaxRunTime;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidMaxRunTimeException extends CodedException {

  private final int value;

  public InvalidMaxRunTimeException(int value) {
    super(
        QuestionErrorCode.INVALID_MAX_RUN_TIME,
        "Max run time %d is outside %d..%d".formatted(value, MaxRunTime.MIN, MaxRunTime.MAX));
    this.value = value;
  }
}
