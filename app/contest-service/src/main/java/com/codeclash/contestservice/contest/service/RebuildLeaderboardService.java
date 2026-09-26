package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.port.inbound.RebuildLeaderboardUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class RebuildLeaderboardService implements RebuildLeaderboardUseCase {

  private final ContestPersistence contestPersistence;
  private final LeaderboardPersistence leaderboardPersistence;

  private static List<Id> ordered(List<Id> ids) {
    return ids.stream().distinct().sorted(Comparator.comparing(Id::value)).toList();
  }

  @Override
  @Transactional
  public void lock(List<Id> ids) {
    RebuildLeaderboardService.ordered(ids).forEach(this.contestPersistence::lockById);
  }

  @Override
  @Transactional
  public void rebuild(List<Id> ids) {
    RebuildLeaderboardService.ordered(ids).forEach(this::rebuild);
  }

  private void rebuild(Id id) {
    if (this.contestPersistence.lockById(id).isEmpty()) return;
    this.leaderboardPersistence.rebuild(id);
    RebuildLeaderboardService.log
        .atInfo()
        .setMessage("leaderboard rebuilt")
        .addKeyValue("contest.id", id.value())
        .log();
  }
}
