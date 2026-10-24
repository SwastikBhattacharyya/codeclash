package com.codeclash.runnerservice.testcase.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum TestCaseErrorCode implements ErrorCode {
  INVALID_QUESTION_ID("Invalid question id"),
  INVALID_TEST_CASE_ID("Invalid test case id"),
  NULL_INPUT("Input is required"),
  NULL_OUTPUT("Output is required"),
  NULL_PUBLIC("Public is required"),
  NULL_QUESTION_ID("Question id is required"),
  NULL_TEST_CASE_ID("Test case id is required"),
  QUESTION_NOT_FOUND("Question not found");

  private final String title;

  TestCaseErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
