package com.codeclash.problemservice.testcase.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.port.inbound.DeleteTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.DeleteTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteTestCaseSecuredService implements DeleteTestCaseSecuredUseCase {

  private final DeleteTestCaseUseCase deleteTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.TEST_CASE_WRITE)
  public void delete(QuestionId questionId, Id id) {
    this.testCaseVisibilityGuard.requireDeletable(questionId, id);
    this.deleteTestCaseUseCase.delete(questionId, id);
  }
}
