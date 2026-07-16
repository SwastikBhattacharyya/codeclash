package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email that is already taken")
public record EmailProperties(
    @Schema(
            description = "Email address",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ada@example.com")
        String email) {}
