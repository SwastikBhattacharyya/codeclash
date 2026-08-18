package com.codeclash.problemservice.question.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.port.inbound.DeleteQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.DeleteQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteQuestionSecuredService implements DeleteQuestionSecuredUseCase {

  private final DeleteQuestionUseCase deleteQuestionUseCase;
  private final QuestionVisibilityGuard questionVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.QUESTION_WRITE)
  public void delete(Id id) {
    this.questionVisibilityGuard.requireDeletable(id);
    this.deleteQuestionUseCase.delete(id);
  }
}
