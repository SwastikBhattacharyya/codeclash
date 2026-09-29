package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contest name that is already taken")
public record ContestNameProperties(
    @Schema(
            description = "Contest name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "Weekly Round 12")
        String name) {}
