package com.codeclash.contestservice.contest.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.contest.ContestPermissions;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;
import com.codeclash.contestservice.contest.port.inbound.PatchContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.PatchContestUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchContestSecuredService implements PatchContestSecuredUseCase {

  private final PatchContestUseCase patchContestUseCase;
  private final ContestQuestionMasker contestQuestionMasker;

  @Override
  @Authorized(ContestPermissions.CONTEST_WRITE)
  public Contest patch(PatchContestRequest request) {
    return this.contestQuestionMasker.mask(this.patchContestUseCase.patch(request));
  }
}
