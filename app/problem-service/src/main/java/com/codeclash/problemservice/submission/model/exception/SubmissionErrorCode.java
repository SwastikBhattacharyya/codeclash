package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum SubmissionErrorCode implements ErrorCode {
  ACTUAL_OUTPUT_TOO_LONG("Actual output too long"),
  BLANK_CODE("Code is blank"),
  CODE_TOO_LONG("Code too long"),
  COMPILE_OUTPUT_TOO_LONG("Compile output too long"),
  FAILED_TEST_CASE_NOT_FOUND("Failed test case not found"),
  INVALID_FAILED_TEST_CASE_ID("Invalid failed test case id"),
  INVALID_LANGUAGE("Invalid language"),
  INVALID_QUESTION_ID("Invalid question id"),
  INVALID_SUBMISSION_ID("Invalid submission id"),
  INVALID_SUBMISSION_RESULT("Invalid submission result"),
  INVALID_SUBMISSION_STATE("Invalid submission state"),
  INVALID_USER_ID("Invalid user id"),
  NULL_ACTUAL_OUTPUT("Actual output is required"),
  NULL_CODE("Code is required"),
  NULL_COMPILE_OUTPUT("Compile output is required"),
  NULL_FAILED_TEST_CASE_ID("Failed test case id is required"),
  NULL_LANGUAGE("Language is required"),
  NULL_QUESTION_ID("Question id is required"),
  NULL_SUBMISSION_ID("Submission id is required"),
  NULL_USER_ID("User id is required"),
  QUESTION_NOT_FOUND("Question not found"),
  SUBMISSION_NOT_FOUND("Submission not found"),
  USER_NOT_FOUND("User not found");

  private final String title;

  SubmissionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
