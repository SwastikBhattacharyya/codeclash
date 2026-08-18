package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.port.inbound.GetQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.GetQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetQuestionSecuredService implements GetQuestionSecuredUseCase {

  private final GetQuestionUseCase getQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  public Question findById(Id id) {
    Question question = this.getQuestionUseCase.findById(id);
    this.questionVisibilityGuard.requireReadable(question);
    return question;
  }
}
