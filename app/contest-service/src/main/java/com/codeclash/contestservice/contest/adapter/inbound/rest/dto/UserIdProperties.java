package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User the error refers to")
public record UserIdProperties(
    @Schema(
            description = "User id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
        UUID userId) {}
