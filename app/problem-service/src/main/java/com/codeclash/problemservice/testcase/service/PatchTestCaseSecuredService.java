package com.codeclash.problemservice.testcase.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.PatchTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.PatchTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchTestCaseSecuredService implements PatchTestCaseSecuredUseCase {

  private final PatchTestCaseUseCase patchTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  @Authorized(ProblemPermissions.TEST_CASE_WRITE)
  public TestCase patch(PatchTestCaseRequest request) {
    this.testCaseVisibilityGuard.requirePatchable(
        request.questionId(), request.id(), request.isPublic());
    return this.patchTestCaseUseCase.patch(request);
  }
}
