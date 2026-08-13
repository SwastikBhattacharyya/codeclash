package com.codeclash.problemservice.category.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category name the error refers to")
public record CategoryNameProperties(
    @Schema(
            description = "Category name that is already taken",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "graph-theory")
        String name) {}
