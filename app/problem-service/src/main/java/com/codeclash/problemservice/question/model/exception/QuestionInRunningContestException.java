package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class QuestionInRunningContestException extends CodedException {

  private final UUID id;
  private final List<UUID> contestIds;

  public QuestionInRunningContestException(UUID id, List<UUID> contestIds) {
    super(
        QuestionErrorCode.QUESTION_IN_RUNNING_CONTEST,
        "Question %s is in running contests %s".formatted(id, contestIds));
    this.id = id;
    this.contestIds = contestIds;
  }
}
