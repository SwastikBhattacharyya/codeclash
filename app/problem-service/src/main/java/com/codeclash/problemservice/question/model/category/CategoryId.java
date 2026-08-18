package com.codeclash.problemservice.question.model.category;

import com.codeclash.problemservice.question.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.question.model.exception.NullCategoriesException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class CategoryId {

  public static final int VERSION = 7;

  private final UUID value;

  private CategoryId(UUID value) {
    this.value = value;
  }

  public static CategoryId of(UUID value) {
    if (value == null) throw new NullCategoriesException();
    if (value.version() != CategoryId.VERSION) throw new InvalidCategoryIdException(value);
    return new CategoryId(value);
  }

  public static CategoryId unsafe(UUID value) {
    return new CategoryId(value);
  }

  public UUID value() {
    return this.value;
  }
}
