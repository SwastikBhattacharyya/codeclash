package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.exception.QuestionInRunningContestException;
import com.codeclash.problemservice.question.port.outbound.RunningContestLookup;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class QuestionContestRules {

  private final RunningContestLookup runningContestLookup;

  void requireHideable(Id id) {
    this.requireNotRunning(id);
  }

  void requireDeletable(Id id) {
    this.requireNotRunning(id);
  }

  private void requireNotRunning(Id id) {
    List<UUID> contestIds = this.runningContestLookup.findRunningIds(id);
    if (!contestIds.isEmpty()) throw new QuestionInRunningContestException(id.value(), contestIds);
  }
}
