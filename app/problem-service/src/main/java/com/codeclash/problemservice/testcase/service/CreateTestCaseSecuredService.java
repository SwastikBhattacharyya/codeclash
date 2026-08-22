package com.codeclash.problemservice.testcase.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.CreateTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.CreateTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateTestCaseSecuredService implements CreateTestCaseSecuredUseCase {

  private final CreateTestCaseUseCase createTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.TEST_CASE_WRITE)
  public TestCase create(CreateTestCaseRequest request) {
    this.testCaseVisibilityGuard.requireCreatable(request.questionId(), request.isPublic());
    return this.createTestCaseUseCase.create(request);
  }
}
