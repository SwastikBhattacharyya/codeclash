package com.codeclash.problemservice.contest.service;

import com.codeclash.problemservice.contest.api.usecase.FindRunningContestIdsApiUseCase;
import com.codeclash.problemservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ContestApiService implements FindRunningContestIdsApiUseCase {

  private final ContestPersistence contestPersistence;

  @Override
  public List<UUID> findRunningIds(UUID questionId) {
    return this.contestPersistence.findRunningIds(questionId, Instant.now());
  }
}
