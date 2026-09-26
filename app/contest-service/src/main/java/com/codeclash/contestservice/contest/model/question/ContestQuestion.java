package com.codeclash.contestservice.contest.model.question;

import java.util.UUID;

public record ContestQuestion(QuestionId questionId, Points points) {

  public static ContestQuestion of(UUID questionId, Integer points) {
    return new ContestQuestion(QuestionId.of(questionId), Points.of(points));
  }

  public static ContestQuestion unsafe(UUID questionId, int points) {
    return new ContestQuestion(QuestionId.unsafe(questionId), Points.unsafe(points));
  }
}
