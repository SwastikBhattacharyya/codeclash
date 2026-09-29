package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a contest; only the fields sent are changed")
public record PatchContestRequestBody(
    @Schema(
            description =
                "Contest name on a single line, unique ignoring case; surrounding whitespace "
                    + "is trimmed; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Weekly Round 12")
        JsonNullable<String> name,
    @Schema(
            description =
                "Contest description; surrounding whitespace is trimmed; leave out to keep "
                    + "the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Five problems on graphs and dynamic programming, two hours.")
        JsonNullable<String> description,
    @Schema(
            description =
                "When the contest goes live, truncated to microseconds; a new value must be "
                    + "in the future; locked once the contest has started; leave out to keep the "
                    + "current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Instant.class,
            example = "2026-02-01T15:00:00Z")
        JsonNullable<Instant> liveAt,
    @Schema(
            description =
                "When the contest ends, after liveAt, truncated to microseconds; a new value "
                    + "must be in the future; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Instant.class,
            example = "2026-02-01T17:00:00Z")
        JsonNullable<Instant> endAt,
    @Schema(
            description =
                "Questions with their points; no question twice, [] for none; locked once the"
                    + " contest has started; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = ContestQuestionRequestBody[].class)
        JsonNullable<List<ContestQuestionRequestBody>> questions) {

  public PatchContestRequest toRequest(UUID id) {
    return new PatchContestRequest(
        Id.of(id),
        PatchObject.from(this.name).map(Name::of),
        PatchObject.from(this.description).map(Description::of),
        PatchObject.from(this.liveAt).map(LiveAt::new),
        PatchObject.from(this.endAt).map(EndAt::new),
        PatchObject.from(this.questions).map(ContestQuestionRequestBody::toQuestions));
  }
}
