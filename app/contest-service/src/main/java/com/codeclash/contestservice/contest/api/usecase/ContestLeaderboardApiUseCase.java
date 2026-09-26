package com.codeclash.contestservice.contest.api.usecase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ContestLeaderboardApiUseCase {

  void rebuildForSubmission(UUID questionId, UUID userId, Instant submittedAt);

  List<UUID> findRegisteredContestIds(UUID userId);

  void lock(List<UUID> contestIds);

  void rebuild(List<UUID> contestIds);
}
