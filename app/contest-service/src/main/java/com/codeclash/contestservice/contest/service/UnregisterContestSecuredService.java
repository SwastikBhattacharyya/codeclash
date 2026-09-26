package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.UnregisterContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.UnregisterContestUseCase;
import com.codeclash.contracts.auth.session.Authenticated;
import com.codeclash.contracts.auth.session.CurrentSession;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class UnregisterContestSecuredService implements UnregisterContestSecuredUseCase {

  private final UnregisterContestUseCase unregisterContestUseCase;
  private final CurrentSession currentSession;

  @Override
  @Authenticated
  public void unregister(Id id) {
    this.unregisterContestUseCase.unregister(id, UserId.unsafe(this.currentSession.userId()));
  }
}
