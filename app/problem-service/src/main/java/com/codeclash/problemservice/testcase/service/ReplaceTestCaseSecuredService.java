package com.codeclash.problemservice.testcase.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;
import com.codeclash.problemservice.testcase.port.inbound.ReplaceTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.ReplaceTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceTestCaseSecuredService implements ReplaceTestCaseSecuredUseCase {

  private final ReplaceTestCaseUseCase replaceTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.TEST_CASE_WRITE)
  public ReplaceTestCaseResult replace(ReplaceTestCaseRequest request) {
    this.testCaseVisibilityGuard.requireReplaceable(
        request.questionId(), request.id(), request.isPublic());
    return this.replaceTestCaseUseCase.replace(request);
  }
}
