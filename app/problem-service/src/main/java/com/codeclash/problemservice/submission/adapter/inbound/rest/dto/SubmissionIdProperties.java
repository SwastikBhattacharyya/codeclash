package com.codeclash.problemservice.submission.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Submission the error refers to")
public record SubmissionIdProperties(
    @Schema(
            description = "Submission id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e")
        UUID id) {}
