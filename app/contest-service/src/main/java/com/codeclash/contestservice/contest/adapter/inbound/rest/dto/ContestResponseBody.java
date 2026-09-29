package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.result.ContestDetail;
import com.codeclash.contracts.contest.ContestPermissions;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A contest")
public record ContestResponseBody(
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
            description =
                "Questions and their points, sorted by question id; null before the contest starts"
                    + " unless the caller has "
                    + ContestPermissions.CONTEST_READ_QUESTIONS,
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"array", "null"},
            implementation = ContestQuestionResponseBody[].class)
        List<ContestQuestionResponseBody> questions,
    @Schema(
            description =
                "Whether the caller is registered; null for anonymous callers and on write "
                    + "responses",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"boolean", "null"},
            example = "true")
        Boolean registered,
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

  public static ContestResponseBody from(ContestDetail detail) {
    return ContestResponseBody.from(detail.contest(), detail.registered());
  }

  public static ContestResponseBody from(Contest contest) {
    return ContestResponseBody.from(contest, null);
  }

  private static ContestResponseBody from(Contest contest, Boolean registered) {
    return new ContestResponseBody(
        contest.id().value(),
        contest.name().value(),
        contest.description().value(),
        contest.liveAt().value(),
        contest.endAt().value(),
        contest.openedAt() == null ? null : contest.openedAt().value(),
        contest.questions() == null
            ? null
            : contest.questions().items().stream().map(ContestQuestionResponseBody::from).toList(),
        registered,
        contest.createdAt().value(),
        contest.updatedAt().value());
  }
}
