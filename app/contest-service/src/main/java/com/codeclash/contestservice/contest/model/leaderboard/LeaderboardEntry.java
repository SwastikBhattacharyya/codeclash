package com.codeclash.contestservice.contest.model.leaderboard;

import java.time.Instant;
import java.util.UUID;

public record LeaderboardEntry(
    UUID userId, String username, int points, int solved, Instant lastSolveAt, int rank) {}
