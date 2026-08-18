package com.codeclash.problemservice.question.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.QueryQuestionUseCase;
import com.codeclash.problemservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryQuestionService implements QueryQuestionUseCase {

  private final QuestionPersistence questionPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Question> query(
      QueryQuestionRequest request) {
    return this.questionPersistence.query(request);
  }
}
