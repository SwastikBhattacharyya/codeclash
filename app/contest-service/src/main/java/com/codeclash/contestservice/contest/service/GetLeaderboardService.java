package com.codeclash.contestservice.contest.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.leaderboard.LeaderboardEntry;
import com.codeclash.contestservice.contest.port.inbound.GetLeaderboardUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class GetLeaderboardService implements GetLeaderboardUseCase {

  private final ContestPersistence contestPersistence;
  private final LeaderboardPersistence leaderboardPersistence;

  private static PagedEntity<LeaderboardEntry> empty(PaginationRequest pagination) {
    return new PagedEntity<>(
        List.of(), pagination.page().value(), pagination.size().value(), 0, false);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<LeaderboardEntry> findPage(Id id, PaginationRequest pagination) {
    Contest contest =
        this.contestPersistence
            .findById(id)
            .orElseThrow(() -> new ContestNotFoundException(id.value()));
    if (contest.liveAt().value().isAfter(Instant.now()))
      return GetLeaderboardService.empty(pagination);
    return this.leaderboardPersistence.page(id, pagination);
  }
}
