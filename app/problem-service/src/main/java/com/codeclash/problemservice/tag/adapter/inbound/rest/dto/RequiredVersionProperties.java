package com.codeclash.problemservice.tag.adapter.inbound.rest.dto;

import com.codeclash.problemservice.tag.model.Id;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "UUID version the id must have")
public record RequiredVersionProperties(
    @Schema(
            description = "Required UUID version",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "" + Id.VERSION)
        int requiredVersion) {}
