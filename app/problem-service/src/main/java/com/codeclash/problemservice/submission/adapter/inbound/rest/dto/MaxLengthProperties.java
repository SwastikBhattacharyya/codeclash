package com.codeclash.problemservice.submission.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Upper length bound that was exceeded")
public record MaxLengthProperties(
    @Schema(
            description = "Largest accepted length in characters",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
        int max) {}
