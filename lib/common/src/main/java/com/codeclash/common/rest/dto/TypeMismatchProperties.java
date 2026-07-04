package com.codeclash.common.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request value that could not be converted")
public record TypeMismatchProperties(
    @Schema(
            description = "Name of the offending parameter or field",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "id")
        String parameter,
    @Schema(
            description = "Expected type",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "UUID")
        String expected) {}
