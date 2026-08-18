package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.MaxRunTime;
import lombok.Getter;

@Getter
public class MaxRunTimeTooHighException extends CodedException {

  private final Integer value;

  public MaxRunTimeTooHighException(Integer value) {
    super(
        QuestionErrorCode.MAX_RUN_TIME_TOO_HIGH,
        "Max run time must be at most %d".formatted(MaxRunTime.MAX));
    this.value = value;
  }
}
