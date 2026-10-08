package com.codeclash.problemservice.question.adapter.outbound.contest;

import com.codeclash.problemservice.contest.api.usecase.FindRunningContestIdsApiUseCase;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.port.outbound.RunningContestLookup;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionContestApiAdapter implements RunningContestLookup {

  private final FindRunningContestIdsApiUseCase findRunningContestIdsApiUseCase;

  @Override
  public List<UUID> findRunningIds(Id questionId) {
    return this.findRunningContestIdsApiUseCase.findRunningIds(questionId.value());
  }
}
