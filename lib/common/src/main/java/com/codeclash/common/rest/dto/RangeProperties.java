package com.codeclash.common.rest.dto;

import com.codeclash.common.page.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Inclusive bounds that were violated")
public record RangeProperties(
    @Schema(
            description = "Smallest accepted value or length",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "" + Size.MIN)
        int min,
    @Schema(
            description = "Largest accepted value or length",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "" + Size.MAX)
        int max) {}
