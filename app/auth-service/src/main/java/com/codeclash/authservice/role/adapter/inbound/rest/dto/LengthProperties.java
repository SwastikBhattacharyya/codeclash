package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Inclusive length bounds that were violated")
public record LengthProperties(
    @Schema(
            description = "Smallest accepted length in characters",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
        int min,
    @Schema(
            description = "Largest accepted length in characters",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
        int max) {}
