package com.codeclash.contestservice.contest.model.question;

import com.codeclash.contestservice.contest.model.exception.DuplicateQuestionsException;
import com.codeclash.contestservice.contest.model.exception.NullQuestionsException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record ContestQuestions(List<ContestQuestion> items) {

  private static final Comparator<ContestQuestion> ORDER =
      Comparator.comparing(
          question -> question.questionId().value(), ContestQuestions::compareUnsigned);

  public ContestQuestions {
    items = items.stream().sorted(ContestQuestions.ORDER).toList();
  }

  public static ContestQuestions of(List<ContestQuestion> items) {
    if (items == null || items.stream().anyMatch(Objects::isNull))
      throw new NullQuestionsException();
    Set<QuestionId> seen = new HashSet<>();
    List<UUID> duplicates =
        items.stream()
            .map(ContestQuestion::questionId)
            .filter(questionId -> !seen.add(questionId))
            .map(QuestionId::value)
            .distinct()
            .toList();
    if (!duplicates.isEmpty()) throw new DuplicateQuestionsException(duplicates);
    return new ContestQuestions(items);
  }

  public static ContestQuestions unsafe(List<ContestQuestion> items) {
    return new ContestQuestions(items);
  }

  public List<UUID> questionIds() {
    return this.items.stream().map(ContestQuestion::questionId).map(QuestionId::value).toList();
  }

  public List<Integer> points() {
    return this.items.stream().map(ContestQuestion::points).map(Points::value).toList();
  }

  private static int compareUnsigned(UUID left, UUID right) {
    int most = Long.compareUnsigned(left.getMostSignificantBits(), right.getMostSignificantBits());
    if (most != 0) return most;
    return Long.compareUnsigned(left.getLeastSignificantBits(), right.getLeastSignificantBits());
  }
}
