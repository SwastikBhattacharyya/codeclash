package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum QuestionErrorCode implements ErrorCode {
  CATEGORIES_NOT_FOUND("Categories not found"),
  DESCRIPTION_TOO_LONG("Description too long"),
  DESCRIPTION_TOO_SHORT("Description too short"),
  INVALID_CATEGORY_ID("Invalid category id"),
  INVALID_DESCRIPTION("Invalid description"),
  INVALID_QUESTION_ID("Invalid question id"),
  INVALID_SUMMARY("Invalid summary"),
  INVALID_TAG_ID("Invalid tag id"),
  INVALID_TITLE("Invalid title"),
  MAX_RAM_LIMIT_TOO_HIGH("Max ram limit too high"),
  MAX_RAM_LIMIT_TOO_LOW("Max ram limit too low"),
  MAX_RUN_TIME_TOO_HIGH("Max run time too high"),
  MAX_RUN_TIME_TOO_LOW("Max run time too low"),
  NULL_CATEGORIES("Categories are required"),
  NULL_DESCRIPTION("Description is required"),
  NULL_MAX_RAM_LIMIT("Max ram limit is required"),
  NULL_MAX_RUN_TIME("Max run time is required"),
  NULL_QUESTION_ID("Question id is required"),
  NULL_SUMMARY("Summary is required"),
  NULL_TAGS("Tags are required"),
  NULL_TITLE("Title is required"),
  NULL_VISIBLE("Visible is required"),
  QUESTION_IN_RUNNING_CONTEST("Question is in a running contest"),
  QUESTION_NOT_FOUND("Question not found"),
  SUMMARY_TOO_LONG("Summary too long"),
  SUMMARY_TOO_SHORT("Summary too short"),
  TAGS_NOT_FOUND("Tags not found"),
  TITLE_TOO_LONG("Title too long"),
  TITLE_TOO_SHORT("Title too short");

  private final String title;

  QuestionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
