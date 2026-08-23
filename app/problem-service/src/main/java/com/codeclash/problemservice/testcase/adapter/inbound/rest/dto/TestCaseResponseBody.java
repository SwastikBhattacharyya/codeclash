package com.codeclash.problemservice.testcase.adapter.inbound.rest.dto;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "An input and expected output used to judge submissions")
public record TestCaseResponseBody(
    @Schema(
            description = "Test case id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
        UUID id,
    @Schema(
            description = "Input fed to the program",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            maxLength = Input.MAX_SIZE,
            example = "2 3")
        String input,
    @Schema(
            description = "Expected program output",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            maxLength = Output.MAX_SIZE,
            example = "5")
        String output,
    @Schema(
            name = "isPublic",
            description = "Whether anyone can see the test case",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "true")
        @JsonProperty("isPublic")
        boolean isPublic,
    @Schema(
            description = "Id of the question the test case belongs to",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID questionId,
    @Schema(
            description = "When the test case was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the test case was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static TestCaseResponseBody from(TestCase testCase) {
    return new TestCaseResponseBody(
        testCase.id().value(),
        testCase.input().value(),
        testCase.output().value(),
        testCase.isPublic().value(),
        testCase.questionId().value(),
        testCase.createdAt().value(),
        testCase.updatedAt().value());
  }
}
