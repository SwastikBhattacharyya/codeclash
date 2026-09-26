package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.api.usecase.ContestLeaderboardApiUseCase;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.RebuildLeaderboardUseCase;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ContestLeaderboardApiService implements ContestLeaderboardApiUseCase {

  private final LeaderboardPersistence leaderboardPersistence;
  private final RebuildLeaderboardUseCase rebuildLeaderboardUseCase;

  @Override
  public void rebuildForSubmission(UUID questionId, UUID userId, Instant submittedAt) {
    List<Id> candidates = this.leaderboardPersistence.findCandidateIds(questionId, submittedAt);
    if (candidates.isEmpty()) return;
    this.rebuildLeaderboardUseCase.lock(candidates);
    List<Id> ids =
        this.leaderboardPersistence.findAffectedIds(
            questionId, UserId.unsafe(userId), submittedAt);
    if (!ids.isEmpty()) this.rebuildLeaderboardUseCase.rebuild(ids);
  }

  @Override
  public List<UUID> findRegisteredContestIds(UUID userId) {
    return this.leaderboardPersistence.findRegisteredIds(UserId.unsafe(userId)).stream()
        .map(Id::value)
        .toList();
  }

  @Override
  public void lock(List<UUID> contestIds) {
    if (!contestIds.isEmpty())
      this.rebuildLeaderboardUseCase.lock(contestIds.stream().map(Id::unsafe).toList());
  }

  @Override
  public void rebuild(List<UUID> contestIds) {
    if (!contestIds.isEmpty())
      this.rebuildLeaderboardUseCase.rebuild(contestIds.stream().map(Id::unsafe).toList());
  }
}
