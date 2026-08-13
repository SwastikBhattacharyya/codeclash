package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum CategoryErrorCode implements ErrorCode {
  CATEGORIES_NOT_FOUND("Categories not found"),
  CATEGORY_NOT_FOUND("Category not found"),
  DESCRIPTION_TOO_LONG("Description too long"),
  DESCRIPTION_TOO_SHORT("Description too short"),
  DUPLICATE_CATEGORY_NAME("Duplicate category name"),
  INVALID_CATEGORY_ID("Invalid category id"),
  INVALID_DESCRIPTION("Invalid description"),
  INVALID_NAME("Invalid name"),
  NAME_TOO_LONG("Name too long"),
  NAME_TOO_SHORT("Name too short"),
  NULL_CATEGORY_ID("Category id is required"),
  NULL_DESCRIPTION("Description is required"),
  NULL_NAME("Name is required");

  private final String title;

  CategoryErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
