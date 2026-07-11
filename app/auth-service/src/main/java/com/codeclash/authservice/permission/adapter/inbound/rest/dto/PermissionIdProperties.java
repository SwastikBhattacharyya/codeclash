package com.codeclash.authservice.permission.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Permission the error refers to")
public record PermissionIdProperties(
    @Schema(
            description = "Permission id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b")
        UUID id) {}
