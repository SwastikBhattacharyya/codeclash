package com.codeclash.problemservice.question.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.PatchQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.PatchQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchQuestionSecuredService implements PatchQuestionSecuredUseCase {

  private final PatchQuestionUseCase patchQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.QUESTION_WRITE)
  public Question patch(
      PatchQuestionRequest request) {
    this.questionVisibilityGuard.requirePatchable(request.id(), request.visible());
    return this.patchQuestionUseCase.patch(request);
  }
}
