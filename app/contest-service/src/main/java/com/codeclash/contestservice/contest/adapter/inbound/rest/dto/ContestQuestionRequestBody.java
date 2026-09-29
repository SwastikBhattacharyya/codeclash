package com.codeclash.contestservice.contest.adapter.inbound.rest.dto;

import com.codeclash.contestservice.contest.model.question.ContestQuestion;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import com.codeclash.contestservice.contest.model.question.Points;
import com.codeclash.contestservice.contest.model.question.QuestionId;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "A question in the contest and what it is worth")
public record ContestQuestionRequestBody(
    @Schema(
            description = "Question id (UUID version " + QuestionId.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @Schema(
            description = "Points for solving the question",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minimum = "" + Points.MIN,
            maximum = "" + Points.MAX,
            example = "100")
        Integer points) {

  public static ContestQuestions toQuestions(List<ContestQuestionRequestBody> bodies) {
    if (bodies == null) return ContestQuestions.of(null);
    return ContestQuestions.of(
        bodies.stream().map(ContestQuestionRequestBody::toQuestion).toList());
  }

  private static ContestQuestion toQuestion(ContestQuestionRequestBody body) {
    if (body == null) return null;
    return ContestQuestion.of(body.id(), body.points());
  }
}
