package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Submitted category ids; at least one does not exist")
public record CategoryIdsProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "All submitted category ids; at least one of them does not exist",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c"))
        List<UUID> ids) {}
