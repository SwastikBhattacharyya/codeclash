package com.codeclash.problemservice.testcase.adapter.inbound.rest.dto;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a test case; only the fields sent are changed")
public record PatchTestCaseRequestBody(
    @Schema(
            description =
                "Input fed to the program; may be empty; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            maxLength = Input.MAX_SIZE,
            example = "2 3")
        JsonNullable<String> input,
    @Schema(
            description =
                "Expected program output; may be empty; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            maxLength = Output.MAX_SIZE,
            example = "5")
        JsonNullable<String> output,
    @Schema(
            name = "isPublic",
            description =
                "Whether anyone can see the test case; false requires the "
                    + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
                    + " permission; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Boolean.class,
            example = "true")
        @JsonProperty("isPublic")
        JsonNullable<Boolean> isPublic) {

  public PatchTestCaseRequest toRequest(UUID questionId, UUID id) {
    return new PatchTestCaseRequest(
        Id.of(id),
        QuestionId.of(questionId),
        PatchObject.from(this.input).map(Input::of),
        PatchObject.from(this.output).map(Output::of),
        PatchObject.from(this.isPublic).map(Public::new));
  }
}
