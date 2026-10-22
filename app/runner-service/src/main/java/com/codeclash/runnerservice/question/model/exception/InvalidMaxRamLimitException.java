package com.codeclash.runnerservice.question.model.exception;

import com.codeclash.runnerservice.question.model.MaxRamLimit;
import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidMaxRamLimitException extends CodedException {

  private final int value;

  public InvalidMaxRamLimitException(int value) {
    super(
        QuestionErrorCode.INVALID_MAX_RAM_LIMIT,
        "Max ram limit %d is outside %d..%d".formatted(value, MaxRamLimit.MIN, MaxRamLimit.MAX));
    this.value = value;
  }
}
