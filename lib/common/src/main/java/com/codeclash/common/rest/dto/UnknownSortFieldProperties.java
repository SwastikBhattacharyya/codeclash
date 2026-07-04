package com.codeclash.common.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Sort field that does not exist")
public record UnknownSortFieldProperties(
    @Schema(
            description = "Requested sort field",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "rating")
        String field,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Sort fields this endpoint accepts",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(example = "name"))
        List<String> allowed) {}
