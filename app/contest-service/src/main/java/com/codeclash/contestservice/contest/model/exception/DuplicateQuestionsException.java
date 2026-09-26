package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DuplicateQuestionsException extends CodedException {

  private final List<UUID> ids;

  public DuplicateQuestionsException(List<UUID> ids) {
    super(ContestErrorCode.DUPLICATE_QUESTIONS, "Questions listed more than once: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
