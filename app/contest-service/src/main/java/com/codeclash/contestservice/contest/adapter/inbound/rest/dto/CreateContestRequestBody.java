package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Contest to create")
public record CreateContestRequestBody(
    @Schema(
            description =
                "Contest name on a single line, unique ignoring case; surrounding whitespace "
                    + "is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Weekly Round 12")
        String name,
    @Schema(
            description = "Contest description; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Five problems on graphs and dynamic programming, two hours.")
        String description,
    @Schema(
            description =
                "When the contest goes live, truncated to microseconds; must be in the future",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "2026-02-01T15:00:00Z")
        Instant liveAt,
    @Schema(
            description =
                "When the contest ends, after liveAt, truncated to microseconds; must be in "
                    + "the future",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "2026-02-01T17:00:00Z")
        Instant endAt,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Questions with their points; no question twice, [] for none",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.WRITE_ONLY))
        List<ContestQuestionRequestBody> questions) {

  public CreateContestRequest toRequest() {
    return new CreateContestRequest(
        Name.of(this.name),
        Description.of(this.description),
        new LiveAt(this.liveAt),
        new EndAt(this.endAt),
        ContestQuestionRequestBody.toQuestions(this.questions));
  }
}
