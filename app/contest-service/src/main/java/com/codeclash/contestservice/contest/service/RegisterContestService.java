package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.ContestEndedException;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.RegisterContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import com.codeclash.contestservice.contest.port.outbound.RegistrationPersistence;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class RegisterContestService implements RegisterContestUseCase {

  private final ContestPersistence contestPersistence;
  private final RegistrationPersistence registrationPersistence;
  private final LeaderboardPersistence leaderboardPersistence;

  @Override
  @Transactional
  public void register(Id id, UserId userId) {
    Contest contest =
        this.contestPersistence
            .lockById(id)
            .orElseThrow(() -> new ContestNotFoundException(id.value()));
    if (!contest.endAt().value().isAfter(Instant.now()))
      throw new ContestEndedException(id.value(), contest.endAt().value());
    this.registrationPersistence.insert(id, userId);
    this.leaderboardPersistence.rebuild(id);
    RegisterContestService.log
        .atInfo()
        .setMessage("contest registration added")
        .addKeyValue("contest.id", id.value())
        .addKeyValue("user.id", userId.value())
        .log();
  }
}
