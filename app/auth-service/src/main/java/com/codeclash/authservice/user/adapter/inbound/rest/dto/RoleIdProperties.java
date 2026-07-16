package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Role the error refers to")
public record RoleIdProperties(
    @Schema(
            description = "Role id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
        UUID id) {}
