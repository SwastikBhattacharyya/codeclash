package com.codeclash.problemservice.category.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidCategoryIdException extends CodedException {

  private final UUID id;

  public InvalidCategoryIdException(UUID id) {
    super(CategoryErrorCode.INVALID_CATEGORY_ID, "%s is not a valid category id".formatted(id));
    this.id = id;
  }
}
