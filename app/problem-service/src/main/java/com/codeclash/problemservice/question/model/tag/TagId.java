package com.codeclash.problemservice.question.model.tag;

import com.codeclash.problemservice.question.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.question.model.exception.NullTagsException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class TagId {

  public static final int VERSION = 7;

  private final UUID value;

  private TagId(UUID value) {
    this.value = value;
  }

  public static TagId of(UUID value) {
    if (value == null) throw new NullTagsException();
    if (value.version() != TagId.VERSION) throw new InvalidTagIdException(value);
    return new TagId(value);
  }

  public static TagId unsafe(UUID value) {
    return new TagId(value);
  }

  public UUID value() {
    return this.value;
  }
}
