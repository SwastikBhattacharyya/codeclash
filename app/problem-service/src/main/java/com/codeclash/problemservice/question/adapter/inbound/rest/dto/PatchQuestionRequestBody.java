package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.model.tag.Tags;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a question; only the fields sent are changed")
public record PatchQuestionRequestBody(
    @Schema(
            description =
                "Question title on a single line; surrounding whitespace is trimmed; leave "
                    + "out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Title.MIN_SIZE,
            maxLength = Title.MAX_SIZE,
            example = "Two Sum Pairs")
        JsonNullable<String> title,
    @Schema(
            description =
                "Short summary shown in question lists; surrounding whitespace is trimmed; "
                    + "leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Summary.MIN_SIZE,
            maxLength = Summary.MAX_SIZE,
            example = "Find two numbers in an array that add up to a target")
        JsonNullable<String> summary,
    @Schema(
            description =
                "Full problem statement; surrounding whitespace is trimmed; leave out to keep"
                    + " the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Return the indices of the two array elements that add up to the target.")
        JsonNullable<String> description,
    @Schema(
            name = "isVisible",
            description =
                "Whether anyone can see the question; false requires the "
                    + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
                    + " permission; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Boolean.class,
            example = "true")
        @JsonProperty("isVisible")
        JsonNullable<Boolean> visible,
    @Schema(
            description =
                "Time limit per test case in milliseconds; leave out to keep the current "
                    + "value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Integer.class,
            minimum = "" + MaxRunTime.MIN,
            maximum = "" + MaxRunTime.MAX,
            example = "" + MaxRunTime.DEFAULT)
        JsonNullable<Integer> maxRunTimeMs,
    @Schema(
            description =
                "Memory limit per test case in megabytes; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Integer.class,
            minimum = "" + MaxRamLimit.MIN,
            maximum = "" + MaxRamLimit.MAX,
            example = "" + MaxRamLimit.DEFAULT)
        JsonNullable<Integer> maxRamLimitMb,
    @Schema(
            implementation = UUID[].class,
            description =
                "Ids of the categories the question belongs to; duplicates are "
                    + "ignored; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "[\"01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c\"]")
        JsonNullable<List<UUID>> categories,
    @Schema(
            implementation = UUID[].class,
            description =
                "Ids of the tags on the question; duplicates are ignored; leave out "
                    + "to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "[\"01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d\"]")
        JsonNullable<List<UUID>> tags) {

  public PatchQuestionRequest toRequest(UUID id) {
    return new PatchQuestionRequest(
        Id.of(id),
        PatchObject.from(this.title).map(Title::of),
        PatchObject.from(this.summary).map(Summary::of),
        PatchObject.from(this.description).map(Description::of),
        PatchObject.from(this.visible).map(Visible::new),
        PatchObject.from(this.maxRunTimeMs).map(MaxRunTime::of),
        PatchObject.from(this.maxRamLimitMb).map(MaxRamLimit::of),
        PatchObject.from(this.categories).map(Categories::of),
        PatchObject.from(this.tags).map(Tags::of));
  }
}
