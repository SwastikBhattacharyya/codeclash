package com.codeclash.problemservice.testcase.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Test case the error refers to")
public record TestCaseIdProperties(
    @Schema(
            description = "Test case id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
        UUID id) {}
