package com.codeclash.common.rest.dto;

import com.codeclash.common.page.Page;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lower bound that was violated")
public record MinProperties(
    @Schema(
            description = "Smallest accepted value",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "" + Page.MIN)
        int min) {}
