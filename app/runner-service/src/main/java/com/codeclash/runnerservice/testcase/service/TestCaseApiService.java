package com.codeclash.runnerservice.testcase.service;

import com.codeclash.runnerservice.testcase.api.TestCaseView;
import com.codeclash.runnerservice.testcase.api.usecase.FindTestCasesByQuestionApiUseCase;
import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import com.codeclash.runnerservice.testcase.port.inbound.FindTestCasesUseCase;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class TestCaseApiService implements FindTestCasesByQuestionApiUseCase {

  private final FindTestCasesUseCase findTestCasesUseCase;

  private static TestCaseView toView(TestCase testCase) {
    return new TestCaseView(
        testCase.id().value(),
        testCase.input().value(),
        testCase.output().value(),
        testCase.isPublic().value());
  }

  @Override
  public List<TestCaseView> findByQuestionId(UUID questionId) {
    return this.findTestCasesUseCase.findByQuestionId(QuestionId.unsafe(questionId)).stream()
        .map(TestCaseApiService::toView)
        .toList();
  }
}
