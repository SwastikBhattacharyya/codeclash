package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationNotFoundException;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.UnregisterContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import com.codeclash.contestservice.contest.port.outbound.RegistrationPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class UnregisterContestService implements UnregisterContestUseCase {

  private final ContestPersistence contestPersistence;
  private final RegistrationPersistence registrationPersistence;
  private final LeaderboardPersistence leaderboardPersistence;
  private final ContestScheduleRules contestScheduleRules;

  @Override
  @Transactional
  public void unregister(Id id, UserId userId) {
    Contest contest =
        this.contestPersistence
            .lockById(id)
            .orElseThrow(() -> new ContestNotFoundException(id.value()));
    if (!this.registrationPersistence.delete(id, userId))
      throw new RegistrationNotFoundException(id.value(), userId.value());
    this.contestScheduleRules.requireDeletable(contest);
    this.leaderboardPersistence.rebuild(id);
    UnregisterContestService.log
        .atInfo()
        .setMessage("contest registration removed")
        .addKeyValue("contest.id", id.value())
        .addKeyValue("user.id", userId.value())
        .log();
  }
}
