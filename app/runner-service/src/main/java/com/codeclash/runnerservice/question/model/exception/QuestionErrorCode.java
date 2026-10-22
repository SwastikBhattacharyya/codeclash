package com.codeclash.runnerservice.question.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum QuestionErrorCode implements ErrorCode {
  INVALID_MAX_RAM_LIMIT("Invalid max ram limit"),
  INVALID_MAX_RUN_TIME("Invalid max run time"),
  INVALID_QUESTION_ID("Invalid question id"),
  NULL_MAX_RAM_LIMIT("Max ram limit is required"),
  NULL_MAX_RUN_TIME("Max run time is required"),
  NULL_QUESTION_ID("Question id is required"),
  QUESTION_NOT_FOUND("Question not found");

  private final String title;

  QuestionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
