package com.codeclash.problemservice.question.model.category;

import com.codeclash.problemservice.question.model.exception.NullCategoriesException;
import java.util.List;
import java.util.UUID;

public record Categories(List<CategoryId> ids) {

  public Categories {
    ids = ids.stream().distinct().toList();
  }

  public static Categories of(List<UUID> ids) {
    if (ids == null) throw new NullCategoriesException();
    return new Categories(ids.stream().map(CategoryId::of).toList());
  }

  public static Categories unsafe(List<UUID> ids) {
    return new Categories(ids.stream().map(CategoryId::unsafe).toList());
  }
}
