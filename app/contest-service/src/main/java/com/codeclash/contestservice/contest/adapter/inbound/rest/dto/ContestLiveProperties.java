package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Contest that has started and is locked")
public record ContestLiveProperties(
    @Schema(
            description = "Contest id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
        UUID id,
    @Schema(
            description = "When the contest went live",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-02-01T15:00:00Z")
        Instant liveAt) {}
