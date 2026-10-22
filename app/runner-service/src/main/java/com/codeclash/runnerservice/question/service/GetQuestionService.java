package com.codeclash.runnerservice.question.service;

import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.Question;
import com.codeclash.runnerservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.runnerservice.question.port.inbound.GetQuestionUseCase;
import com.codeclash.runnerservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetQuestionService implements GetQuestionUseCase {

  private final QuestionPersistence questionPersistence;

  @Override
  public Question findById(Id id) {
    return this.questionPersistence
        .findById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id.value()));
  }
}
