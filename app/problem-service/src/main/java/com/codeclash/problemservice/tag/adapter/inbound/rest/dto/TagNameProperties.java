package com.codeclash.problemservice.tag.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tag name the error refers to")
public record TagNameProperties(
    @Schema(
            description = "Tag name that is already taken",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "dynamic-programming")
        String name) {}
