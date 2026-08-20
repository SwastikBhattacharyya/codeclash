package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Question and the running contests that block the change")
public record QuestionRunningContestProperties(
    @Schema(
            description = "Question id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Ids of the running contests that include the question",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f40-1c2d-7e3f-8a4b-5c6d7e8f9a0b"))
        List<UUID> contestIds) {}
