package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Schedule that was rejected")
public record ScheduleProperties(
    @Schema(
            description = "Requested live time; null when not sent on patch",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "date-time",
            example = "2026-02-01T15:00:00Z")
        Instant liveAt,
    @Schema(
            description = "Requested end time; null when not sent on patch",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "date-time",
            example = "2026-02-01T14:00:00Z")
        Instant endAt) {}
