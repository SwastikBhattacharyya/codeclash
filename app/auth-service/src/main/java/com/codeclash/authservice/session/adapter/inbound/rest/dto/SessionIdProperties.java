package com.codeclash.authservice.session.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Session the error refers to")
public record SessionIdProperties(
    @Schema(
            description = "Session id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d")
        UUID id) {}
