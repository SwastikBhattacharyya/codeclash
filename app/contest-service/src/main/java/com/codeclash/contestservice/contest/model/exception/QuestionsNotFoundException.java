package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class QuestionsNotFoundException extends CodedException {

  private final List<UUID> ids;

  public QuestionsNotFoundException(List<UUID> ids) {
    super(ContestErrorCode.QUESTIONS_NOT_FOUND, "Questions not found: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
