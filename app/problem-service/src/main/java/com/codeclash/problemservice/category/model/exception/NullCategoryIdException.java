package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullCategoryIdException extends CodedException {

  public NullCategoryIdException() {
    super(CategoryErrorCode.NULL_CATEGORY_ID, "Category Id cannot be null");
  }
}
