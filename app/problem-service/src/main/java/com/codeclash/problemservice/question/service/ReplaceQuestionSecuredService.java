package com.codeclash.problemservice.question.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.request.ReplaceQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;
import com.codeclash.problemservice.question.port.inbound.ReplaceQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.ReplaceQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceQuestionSecuredService implements ReplaceQuestionSecuredUseCase {

  private final ReplaceQuestionUseCase replaceQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.QUESTION_WRITE)
  public ReplaceQuestionResult replace(
      ReplaceQuestionRequest request) {
    this.questionVisibilityGuard.requireReplaceable(request.id(), request.visible());
    return this.replaceQuestionUseCase.replace(request);
  }
}
