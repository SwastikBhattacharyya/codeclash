package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullMaxRamLimitException extends CodedException {

  public NullMaxRamLimitException() {
    super(QuestionErrorCode.NULL_MAX_RAM_LIMIT, "Max ram limit cannot be null");
  }
}
