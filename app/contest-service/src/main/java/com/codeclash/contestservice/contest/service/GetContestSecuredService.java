package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.model.result.ContestDetail;
import com.codeclash.contestservice.contest.port.inbound.GetContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.GetContestUseCase;
import com.codeclash.contestservice.contest.port.inbound.GetRegistrationUseCase;
import com.codeclash.contracts.auth.session.CurrentSession;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetContestSecuredService implements GetContestSecuredUseCase {

  private final GetContestUseCase getContestUseCase;
  private final GetRegistrationUseCase getRegistrationUseCase;
  private final ContestQuestionMasker contestQuestionMasker;
  private final CurrentSession currentSession;

  @Override
  public ContestDetail findById(Id id) {
    return new ContestDetail(
        this.contestQuestionMasker.mask(this.getContestUseCase.findById(id)),
        this.registered(id));
  }

  private Boolean registered(Id id) {
    if (!this.currentSession.isAuthenticated()) return null;
    return this.getRegistrationUseCase.isRegistered(
        id, UserId.unsafe(this.currentSession.userId()));
  }
}
