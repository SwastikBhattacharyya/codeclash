package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CategoriesNotFoundException extends CodedException {

  private final List<UUID> ids;

  public CategoriesNotFoundException(List<UUID> ids) {
    super(QuestionErrorCode.CATEGORIES_NOT_FOUND, "Categories not found: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
