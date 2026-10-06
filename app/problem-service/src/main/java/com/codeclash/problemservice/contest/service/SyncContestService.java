package com.codeclash.problemservice.contest.service;

import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.model.Id;
import com.codeclash.problemservice.contest.model.result.SyncContestResult;
import com.codeclash.problemservice.contest.port.inbound.SyncContestUseCase;
import com.codeclash.problemservice.contest.port.outbound.ContestOpenedPublisher;
import com.codeclash.problemservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncContestService implements SyncContestUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestOpenedPublisher contestOpenedPublisher;

  @Override
  @Transactional
  public void write(Contest contest) {
    this.contestPersistence.upsert(contest).ifPresent(result -> this.applied(contest, result));
  }

  @Override
  @Transactional
  public void delete(Id id) {
    this.contestPersistence.deleteById(id);
    SyncContestService.log
        .atDebug()
        .setMessage("contest removed")
        .addKeyValue("contest.id", id.value())
        .log();
  }

  private void applied(Contest contest, SyncContestResult result) {
    this.contestPersistence.replaceQuestions(contest.id(), contest.questionIds());
    if (result.opened()) this.contestOpenedPublisher.opened(contest);
    SyncContestService.log
        .atDebug()
        .setMessage("contest synced")
        .addKeyValue("contest.id", contest.id().value())
        .addKeyValue("opened", result.opened())
        .log();
  }
}
