package com.codeclash.problemservice.question.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.QueryQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.QueryQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QueryQuestionSecuredService implements QueryQuestionSecuredUseCase {

  private final QueryQuestionUseCase queryQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  public PagedEntity<Question> query(QueryQuestionRequest request) {
    return this.queryQuestionUseCase.query(
        this.questionVisibilityGuard.visibleFilter(request));
  }
}
