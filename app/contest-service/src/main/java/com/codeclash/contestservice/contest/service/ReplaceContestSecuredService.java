package com.codeclash.contestservice.contest.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.contest.ContestPermissions;
import com.codeclash.contestservice.contest.model.request.ReplaceContestRequest;
import com.codeclash.contestservice.contest.model.result.ReplaceContestResult;
import com.codeclash.contestservice.contest.port.inbound.ReplaceContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.ReplaceContestUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceContestSecuredService implements ReplaceContestSecuredUseCase {

  private final ReplaceContestUseCase replaceContestUseCase;
  private final ContestQuestionMasker contestQuestionMasker;

  @Override
  @Authorized(ContestPermissions.CONTEST_WRITE)
  public ReplaceContestResult replace(ReplaceContestRequest request) {
    ReplaceContestResult result = this.replaceContestUseCase.replace(request);
    return new ReplaceContestResult(
        this.contestQuestionMasker.mask(result.contest()), result.created());
  }
}
