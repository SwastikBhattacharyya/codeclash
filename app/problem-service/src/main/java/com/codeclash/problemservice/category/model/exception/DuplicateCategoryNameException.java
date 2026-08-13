package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class DuplicateCategoryNameException extends CodedException {

  private final String name;

  public DuplicateCategoryNameException(String name) {
    super(
        CategoryErrorCode.DUPLICATE_CATEGORY_NAME,
        "Category with name %s already exists".formatted(name));
    this.name = name;
  }
}
