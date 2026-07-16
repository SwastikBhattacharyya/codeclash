package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Username that is already taken")
public record UsernameProperties(
    @Schema(
            description = "Username",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ada_lovelace")
        String username) {}
