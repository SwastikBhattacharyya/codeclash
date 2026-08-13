package com.codeclash.problemservice.category.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Category the error refers to")
public record CategoryIdProperties(
    @Schema(
            description = "Category id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id) {}
