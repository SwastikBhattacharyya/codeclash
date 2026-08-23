package com.codeclash.problemservice.testcase.adapter.inbound.rest.dto;

import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description =
        "Full replacement of a test case; omitted isPublic means false, and the test case is "
            + "created when the id does not exist")
public record ReplaceTestCaseRequestBody(
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
                    + " permission; omitted means false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            defaultValue = "false",
            example = "true")
        @JsonProperty("isPublic")
        Boolean isPublic) {

  public ReplaceTestCaseRequest toRequest(UUID questionId, UUID id) {
    return new ReplaceTestCaseRequest(
        Id.of(id),
        QuestionId.of(questionId),
        Input.of(this.input),
        Output.of(this.output),
        new Public(this.isPublic != null && this.isPublic));
  }
}
