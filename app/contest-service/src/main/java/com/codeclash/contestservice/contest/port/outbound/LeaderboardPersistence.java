package com.codeclash.contestservice.contest.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.leaderboard.LeaderboardEntry;
import com.codeclash.contestservice.contest.model.registration.UserId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LeaderboardPersistence {

  void rebuild(Id id);

  PagedEntity<LeaderboardEntry> page(Id id, PaginationRequest pagination);

  List<Id> findCandidateIds(UUID questionId, Instant submittedAt);

  List<Id> findAffectedIds(UUID questionId, UserId userId, Instant submittedAt);

  List<Id> findRegisteredIds(UserId userId);
}
