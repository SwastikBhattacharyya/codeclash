package com.codeclash.problemservice.question.adapter.inbound.rest.dto;

import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.tag.TagId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A coding question")
public record QuestionResponseBody(
    @Schema(
            description = "Question id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @Schema(
            description = "Question title",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Title.MIN_SIZE,
            maxLength = Title.MAX_SIZE,
            example = "Two Sum Pairs")
        String title,
    @Schema(
            description = "Short summary shown in question lists",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Summary.MIN_SIZE,
            maxLength = Summary.MAX_SIZE,
            example = "Find two numbers in an array that add up to a target")
        String summary,
    @Schema(
            description = "Full problem statement",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Return the indices of the two array elements that add up to the target.")
        String description,
    @Schema(
            name = "isVisible",
            description = "Whether anyone can see the question",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "true")
        @JsonProperty("isVisible")
        boolean visible,
    @Schema(
            description = "Time limit per test case in milliseconds",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "" + MaxRunTime.MIN,
            maximum = "" + MaxRunTime.MAX,
            example = "" + MaxRunTime.DEFAULT)
        int maxRunTimeMs,
    @Schema(
            description = "Memory limit per test case in megabytes",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "" + MaxRamLimit.MIN,
            maximum = "" + MaxRamLimit.MAX,
            example = "" + MaxRamLimit.DEFAULT)
        int maxRamLimitMb,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Ids of the categories the question belongs to",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c"))
        List<UUID> categories,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Ids of the tags on the question",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d"))
        List<UUID> tags,
    @Schema(
            description = "When the question was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the question was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static QuestionResponseBody from(Question question) {
    return new QuestionResponseBody(
        question.id().value(),
        question.title().value(),
        question.summary().value(),
        question.description().value(),
        question.visible().value(),
        question.maxRunTime().value(),
        question.maxRamLimit().value(),
        question.categories().ids().stream().map(CategoryId::value).toList(),
        question.tags().ids().stream().map(TagId::value).toList(),
        question.createdAt().value(),
        question.updatedAt().value());
  }
}
