package com.codeclash.contestservice.submission.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum SubmissionErrorCode implements ErrorCode {
  INVALID_SUBMISSION_ID("Invalid submission id"),
  NULL_SUBMISSION_ID("Submission id is required");

  private final String title;

  SubmissionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
