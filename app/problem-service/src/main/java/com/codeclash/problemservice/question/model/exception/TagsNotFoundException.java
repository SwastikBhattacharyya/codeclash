package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TagsNotFoundException extends CodedException {

  private final List<UUID> ids;

  public TagsNotFoundException(List<UUID> ids) {
    super(QuestionErrorCode.TAGS_NOT_FOUND, "Tags not found: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
