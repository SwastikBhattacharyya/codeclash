package com.codeclash.problemservice.tag.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum TagErrorCode implements ErrorCode {
  DESCRIPTION_TOO_LONG("Description too long"),
  DESCRIPTION_TOO_SHORT("Description too short"),
  DUPLICATE_TAG_NAME("Duplicate tag name"),
  INVALID_DESCRIPTION("Invalid description"),
  INVALID_NAME("Invalid name"),
  INVALID_TAG_ID("Invalid tag id"),
  NAME_TOO_LONG("Name too long"),
  NAME_TOO_SHORT("Name too short"),
  NULL_DESCRIPTION("Description is required"),
  NULL_NAME("Name is required"),
  NULL_TAG_ID("Tag id is required"),
  TAGS_NOT_FOUND("Tags not found"),
  TAG_NOT_FOUND("Tag not found");

  private final String title;

  TagErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
