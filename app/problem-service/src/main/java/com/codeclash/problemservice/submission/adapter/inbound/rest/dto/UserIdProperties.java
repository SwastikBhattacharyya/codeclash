package com.codeclash.problemservice.submission.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User the error refers to")
public record UserIdProperties(
    @Schema(
            description = "User id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f51-5a6b-7c7d-9e8f-9a0b1c2d3e4f")
        UUID id) {}
