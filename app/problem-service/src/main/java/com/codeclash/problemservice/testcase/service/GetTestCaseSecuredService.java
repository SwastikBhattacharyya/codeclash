package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.port.inbound.GetTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.GetTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetTestCaseSecuredService implements GetTestCaseSecuredUseCase {

  private final GetTestCaseUseCase getTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  public TestCase findById(QuestionId questionId, Id id) {
    this.testCaseVisibilityGuard.requireQuestionVisible(questionId);
    TestCase testCase = this.getTestCaseUseCase.findById(questionId, id);
    this.testCaseVisibilityGuard.requireVisible(testCase);
    return testCase;
  }
}
