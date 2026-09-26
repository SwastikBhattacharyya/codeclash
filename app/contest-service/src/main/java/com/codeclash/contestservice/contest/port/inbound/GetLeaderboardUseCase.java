package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.leaderboard.LeaderboardEntry;

public interface GetLeaderboardUseCase {

  PagedEntity<LeaderboardEntry> findPage(Id id, PaginationRequest pagination);
}
