package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CategoryNotFoundException extends CodedException {

  private final UUID id;

  public CategoryNotFoundException(UUID id) {
    super(CategoryErrorCode.CATEGORY_NOT_FOUND, "Category with id %s not found".formatted(id));
    this.id = id;
  }
}
