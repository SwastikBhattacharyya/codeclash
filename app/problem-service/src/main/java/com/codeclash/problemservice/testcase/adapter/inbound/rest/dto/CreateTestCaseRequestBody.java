package com.codeclash.problemservice.testcase.adapter.inbound.rest.dto;

import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Test case to add to the question")
public record CreateTestCaseRequestBody(
    @Schema(
            description = "Input fed to the program; may be empty",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            maxLength = Input.MAX_SIZE,
            example = "2 3")
        String input,
    @Schema(
            description = "Expected program output; may be empty",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            maxLength = Output.MAX_SIZE,
            example = "5")
        String output,
    @Schema(
            name = "isPublic",
            description =
                "Whether anyone can see the test case; false requires the "
                    + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
                    + " permission; defaults to false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            defaultValue = "false",
            example = "true")
        @JsonProperty("isPublic")
        Boolean isPublic) {

  public CreateTestCaseRequest toRequest(UUID questionId) {
    return new CreateTestCaseRequest(
        QuestionId.of(questionId),
        Input.of(this.input),
        Output.of(this.output),
        new Public(this.isPublic != null && this.isPublic));
  }
}
