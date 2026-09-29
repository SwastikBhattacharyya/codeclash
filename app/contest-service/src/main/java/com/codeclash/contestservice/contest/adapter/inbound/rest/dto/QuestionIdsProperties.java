package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Question ids the error refers to")
public record QuestionIdsProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "For QUESTIONS_NOT_FOUND all submitted question ids, at least one of "
                            + "which does not exist; for DUPLICATE_QUESTIONS the ids listed more "
                            + "than once",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))
        List<UUID> ids) {}
