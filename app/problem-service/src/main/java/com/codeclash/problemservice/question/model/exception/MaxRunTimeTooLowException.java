package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.MaxRunTime;
import lombok.Getter;

@Getter
public class MaxRunTimeTooLowException extends CodedException {

  private final Integer value;

  public MaxRunTimeTooLowException(Integer value) {
    super(
        QuestionErrorCode.MAX_RUN_TIME_TOO_LOW,
        "Max run time must be at least %d".formatted(MaxRunTime.MIN));
    this.value = value;
  }
}
