package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.port.inbound.DeleteContestUseCase;
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
public class DeleteContestService implements DeleteContestUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestScheduleRules contestScheduleRules;
  private final ContestEventPublisher contestEventPublisher;

  @Override
  @Transactional
  public void delete(Id id) {
    Contest contest =
        this.contestPersistence
            .deleteById(id)
            .orElseThrow(() -> new ContestNotFoundException(id.value()));
    this.contestScheduleRules.requireDeletable(contest);
    this.contestEventPublisher.deleted(contest);
    DeleteContestService.log
        .atInfo()
        .setMessage("contest deleted")
        .addKeyValue("contest.id", contest.id().value())
        .log();
  }
}
