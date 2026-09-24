package com.codeclash.contestservice.question.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum QuestionErrorCode implements ErrorCode {
  INVALID_QUESTION_ID("Invalid question id"),
  NULL_QUESTION_ID("Question id is required");

  private final String title;

  QuestionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
