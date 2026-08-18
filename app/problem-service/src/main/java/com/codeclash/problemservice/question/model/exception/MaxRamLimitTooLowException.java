package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import lombok.Getter;

@Getter
public class MaxRamLimitTooLowException extends CodedException {

  private final Integer value;

  public MaxRamLimitTooLowException(Integer value) {
    super(
        QuestionErrorCode.MAX_RAM_LIMIT_TOO_LOW,
        "Max ram limit must be at least %d".formatted(MaxRamLimit.MIN));
    this.value = value;
  }
}
