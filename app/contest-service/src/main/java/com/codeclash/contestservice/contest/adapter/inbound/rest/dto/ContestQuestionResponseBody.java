package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.question.ContestQuestion;
import com.codeclash.contestservice.contest.model.question.Points;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "A question in the contest and what it is worth")
public record ContestQuestionResponseBody(
    @Schema(
            description = "Question id",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @Schema(
            description = "Points for solving the question",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "" + Points.MIN,
            maximum = "" + Points.MAX,
            example = "100")
        int points) {

  public static ContestQuestionResponseBody from(ContestQuestion question) {
    return new ContestQuestionResponseBody(
        question.questionId().value(), question.points().value());
  }
}
