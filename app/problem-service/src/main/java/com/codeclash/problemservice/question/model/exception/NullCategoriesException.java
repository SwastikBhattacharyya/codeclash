package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullCategoriesException extends CodedException {

  public NullCategoriesException() {
    super(QuestionErrorCode.NULL_CATEGORIES, "Categories cannot be null or contain null");
  }
}
