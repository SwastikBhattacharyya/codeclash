package com.codeclash.contestservice.contest.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.contest.ContestPermissions;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;
import com.codeclash.contestservice.contest.port.inbound.CreateContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.CreateContestUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateContestSecuredService implements CreateContestSecuredUseCase {

  private final CreateContestUseCase createContestUseCase;
  private final ContestQuestionMasker contestQuestionMasker;

  @Override
  @Authorized(ContestPermissions.CONTEST_WRITE)
  public Contest create(CreateContestRequest request) {
    return this.contestQuestionMasker.mask(this.createContestUseCase.create(request));
  }
}
