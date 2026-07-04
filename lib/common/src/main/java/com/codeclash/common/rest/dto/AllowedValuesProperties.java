package com.codeclash.common.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Values the field accepts")
public record AllowedValuesProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Accepted values",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(example = "asc"))
        List<String> allowed) {}
