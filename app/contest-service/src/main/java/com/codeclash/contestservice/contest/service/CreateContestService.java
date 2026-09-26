package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;
import com.codeclash.contestservice.contest.port.inbound.CreateContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestEventPublisher;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateContestService implements CreateContestUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestScheduleRules contestScheduleRules;
  private final ContestEventPublisher contestEventPublisher;

  @Override
  @Transactional
  public Contest create(CreateContestRequest request) {
    this.contestScheduleRules.requireCreatable(request.liveAt(), request.endAt());
    Contest contest = this.contestPersistence.create(request);
    if (!request.questions().items().isEmpty())
      this.contestPersistence.replaceQuestions(contest.id(), request.questions());
    Contest created = contest.withQuestions(request.questions());
    this.contestEventPublisher.written(created);
    CreateContestService.log
        .atInfo()
        .setMessage("contest created")
        .addKeyValue("contest.id", created.id().value())
        .log();
    return created;
  }
}
