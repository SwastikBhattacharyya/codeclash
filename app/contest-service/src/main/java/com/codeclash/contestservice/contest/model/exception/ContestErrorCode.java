package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum ContestErrorCode implements ErrorCode {
  ALREADY_REGISTERED("Already registered"),
  CONTEST_ALREADY_LIVE("Contest already live"),
  CONTEST_ENDED("Contest ended"),
  CONTEST_NOT_FOUND("Contest not found"),
  DESCRIPTION_TOO_LONG("Description too long"),
  DESCRIPTION_TOO_SHORT("Description too short"),
  DUPLICATE_CONTEST_NAME("Duplicate contest name"),
  DUPLICATE_QUESTIONS("Duplicate questions"),
  END_AT_IN_PAST("End time in the past"),
  END_AT_NOT_AFTER_LIVE_AT("End time not after live time"),
  INVALID_CONTEST_ID("Invalid contest id"),
  INVALID_DESCRIPTION("Invalid description"),
  INVALID_NAME("Invalid name"),
  INVALID_QUESTION_ID("Invalid question id"),
  LIVE_AT_IN_PAST("Live time in the past"),
  NAME_TOO_LONG("Name too long"),
  NAME_TOO_SHORT("Name too short"),
  NULL_CONTEST_ID("Contest id is required"),
  NULL_DESCRIPTION("Description is required"),
  NULL_END_AT("End time is required"),
  NULL_LIVE_AT("Live time is required"),
  NULL_NAME("Name is required"),
  NULL_POINTS("Points are required"),
  NULL_QUESTION_ID("Question id is required"),
  NULL_QUESTIONS("Questions are required"),
  POINTS_TOO_HIGH("Points too high"),
  POINTS_TOO_LOW("Points too low"),
  QUESTIONS_NOT_FOUND("Questions not found"),
  REGISTRATION_NOT_FOUND("Registration not found"),
  REGISTRATION_USER_NOT_FOUND("Registration user not found");

  private final String title;

  ContestErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
