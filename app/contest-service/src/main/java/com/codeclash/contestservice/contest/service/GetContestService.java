package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.port.inbound.GetContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetContestService implements GetContestUseCase {

  private final ContestPersistence contestPersistence;

  @Override
  public Contest findById(Id id) {
    return this.contestPersistence
        .findById(id)
        .orElseThrow(() -> new ContestNotFoundException(id.value()));
  }
}
