package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.leaderboard.LeaderboardEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "A registered user's standing in the contest")
public record LeaderboardEntryResponseBody(
    @Schema(
            description =
                "Rank by points, then earliest last solve; ties share a rank and leave a gap",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "1")
        int rank,
    @Schema(
            description = "Username of the user",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ada_lovelace")
        String username,
    @Schema(
            description = "Sum of the points of the questions solved",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "300")
        int points,
    @Schema(
            description = "Number of questions solved",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "3")
        int solved,
    @Schema(
            description = "When the last scoring solve happened; null when nothing is solved",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "date-time",
            example = "2026-02-01T16:12:00Z")
        Instant lastSolveAt) {

  public static LeaderboardEntryResponseBody from(LeaderboardEntry entry) {
    return new LeaderboardEntryResponseBody(
        entry.rank(), entry.username(), entry.points(), entry.solved(), entry.lastSolveAt());
  }
}
