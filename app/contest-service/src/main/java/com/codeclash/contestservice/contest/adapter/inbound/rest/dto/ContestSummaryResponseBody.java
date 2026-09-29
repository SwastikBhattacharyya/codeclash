package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.Name;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A contest without its questions")
public record ContestSummaryResponseBody(
    @Schema(
            description = "Contest id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
        UUID id,
    @Schema(
            description = "Contest name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Weekly Round 12")
        String name,
    @Schema(
            description = "Contest description",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Five problems on graphs and dynamic programming, two hours.")
        String description,
    @Schema(
            description = "When the contest goes live",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-02-01T15:00:00Z")
        Instant liveAt,
    @Schema(
            description = "When the contest ends",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-02-01T17:00:00Z")
        Instant endAt,
    @Schema(
            description =
                "When the contest was opened, about 30 seconds before liveAt; null until "
                    + "then. Once set, liveAt and the questions are locked",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "date-time",
            example = "2026-02-01T14:59:30Z")
        Instant openedAt,
    @Schema(
            description = "When the contest was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the contest was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static ContestSummaryResponseBody from(Contest contest) {
    return new ContestSummaryResponseBody(
        contest.id().value(),
        contest.name().value(),
        contest.description().value(),
        contest.liveAt().value(),
        contest.endAt().value(),
        contest.openedAt() == null ? null : contest.openedAt().value(),
        contest.createdAt().value(),
        contest.updatedAt().value());
  }
}
