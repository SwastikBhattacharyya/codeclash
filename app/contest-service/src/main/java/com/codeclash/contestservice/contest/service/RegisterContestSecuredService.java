package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.RegisterContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.RegisterContestUseCase;
import com.codeclash.contracts.auth.session.Authenticated;
import com.codeclash.contracts.auth.session.CurrentSession;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class RegisterContestSecuredService implements RegisterContestSecuredUseCase {

  private final RegisterContestUseCase registerContestUseCase;
  private final CurrentSession currentSession;

  @Override
  @Authenticated
  public void register(Id id) {
    this.registerContestUseCase.register(id, UserId.unsafe(this.currentSession.userId()));
  }
}
