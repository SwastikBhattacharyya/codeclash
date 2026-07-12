package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role name that is already taken")
public record RoleNameProperties(
    @Schema(
            description = "Role name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "contest_manager")
        String name) {}
