package com.codeclash.problemservice.question.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.CreateQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.CreateQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateQuestionSecuredService implements CreateQuestionSecuredUseCase {

  private final CreateQuestionUseCase createQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.QUESTION_WRITE)
  public Question create(
      CreateQuestionRequest request) {
    this.questionVisibilityGuard.requireCreatable(request.visible());
    return this.createQuestionUseCase.create(request);
  }
}
