package com.codeclash.contestservice.contest.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.contest.ContestPermissions;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.port.inbound.DeleteContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.DeleteContestUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteContestSecuredService implements DeleteContestSecuredUseCase {

  private final DeleteContestUseCase deleteContestUseCase;

  @Override
  @Authorized(ContestPermissions.CONTEST_WRITE)
  public void delete(Id id) {
    this.deleteContestUseCase.delete(id);
  }
}
