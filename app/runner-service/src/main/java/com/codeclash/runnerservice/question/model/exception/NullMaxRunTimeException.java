package com.codeclash.runnerservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullMaxRunTimeException extends CodedException {

  public NullMaxRunTimeException() {
    super(QuestionErrorCode.NULL_MAX_RUN_TIME, "Max run time cannot be null");
  }
}
