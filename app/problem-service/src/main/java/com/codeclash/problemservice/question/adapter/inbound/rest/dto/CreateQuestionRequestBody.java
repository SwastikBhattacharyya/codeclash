package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;
import com.codeclash.problemservice.question.model.tag.Tags;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Question to create")
public record CreateQuestionRequestBody(
    @Schema(
            description = "Question title on a single line; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Title.MIN_SIZE,
            maxLength = Title.MAX_SIZE,
            example = "Two Sum Pairs")
        String title,
    @Schema(
            description =
                "Short summary shown in question lists; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Summary.MIN_SIZE,
            maxLength = Summary.MAX_SIZE,
            example = "Find two numbers in an array that add up to a target")
        String summary,
    @Schema(
            description = "Full problem statement; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Return the indices of the two array elements that add up to the target.")
        String description,
    @Schema(
            name = "isVisible",
            description =
                "Whether anyone can see the question; false requires the "
                    + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
                    + " permission; defaults to false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            defaultValue = "false",
            example = "true")
        @JsonProperty("isVisible")
        Boolean visible,
    @Schema(
            description = "Time limit per test case in milliseconds; defaults to the value shown",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minimum = "" + MaxRunTime.MIN,
            maximum = "" + MaxRunTime.MAX,
            defaultValue = "" + MaxRunTime.DEFAULT,
            example = "" + MaxRunTime.DEFAULT)
        Integer maxRunTimeMs,
    @Schema(
            description = "Memory limit per test case in megabytes; defaults to the value shown",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minimum = "" + MaxRamLimit.MIN,
            maximum = "" + MaxRamLimit.MAX,
            defaultValue = "" + MaxRamLimit.DEFAULT,
            example = "" + MaxRamLimit.DEFAULT)
        Integer maxRamLimitMb,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "Ids of the categories the question belongs to; duplicates are "
                            + "ignored",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.WRITE_ONLY),
            schema = @Schema(format = "uuid", example = "01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c"))
        List<UUID> categories,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Ids of the tags on the question; duplicates are ignored",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.WRITE_ONLY),
            schema = @Schema(format = "uuid", example = "01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d"))
        List<UUID> tags) {

  public CreateQuestionRequest toRequest() {
    return new CreateQuestionRequest(
        Title.of(this.title),
        Summary.of(this.summary),
        Description.of(this.description),
        new Visible(this.visible != null && this.visible),
        this.maxRunTimeMs == null
            ? MaxRunTime.unsafe(MaxRunTime.DEFAULT)
            : MaxRunTime.of(this.maxRunTimeMs),
        this.maxRamLimitMb == null
            ? MaxRamLimit.unsafe(MaxRamLimit.DEFAULT)
            : MaxRamLimit.of(this.maxRamLimitMb),
        Categories.of(this.categories),
        Tags.of(this.tags));
  }
}
