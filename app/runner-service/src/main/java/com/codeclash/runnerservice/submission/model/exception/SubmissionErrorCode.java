package com.codeclash.runnerservice.submission.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum SubmissionErrorCode implements ErrorCode {
  INVALID_QUESTION_ID("Invalid question id"),
  INVALID_SUBMISSION_ID("Invalid submission id"),
  NULL_CODE("Code is required"),
  NULL_LANGUAGE("Language is required"),
  NULL_QUESTION_ID("Question id is required"),
  NULL_SUBMISSION_ID("Submission id is required"),
  QUESTION_NOT_FOUND("Question not found"),
  SANDBOX_FAILURE("Sandbox failure");

  private final String title;

  SubmissionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
