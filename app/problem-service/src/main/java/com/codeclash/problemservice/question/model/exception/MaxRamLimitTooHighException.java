package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import lombok.Getter;

@Getter
public class MaxRamLimitTooHighException extends CodedException {

  private final Integer value;

  public MaxRamLimitTooHighException(Integer value) {
    super(
        QuestionErrorCode.MAX_RAM_LIMIT_TOO_HIGH,
        "Max ram limit must be at most %d".formatted(MaxRamLimit.MAX));
    this.value = value;
  }
}
