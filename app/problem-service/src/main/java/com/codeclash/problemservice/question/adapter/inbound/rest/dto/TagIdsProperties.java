package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Submitted tag ids; at least one does not exist")
public record TagIdsProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "All submitted tag ids; at least one of them does not exist",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d"))
        List<UUID> ids) {}
