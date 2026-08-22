package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.CreateTestCaseUseCase;
import com.codeclash.problemservice.testcase.port.outbound.TestCaseEventPublisher;
import com.codeclash.problemservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateTestCaseService implements CreateTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;
  private final TestCaseEventPublisher testCaseEventPublisher;

  @Override
  @Transactional
  public TestCase create(CreateTestCaseRequest request) {
    TestCase testCase = this.testCasePersistence.create(request);
    this.testCaseEventPublisher.written(testCase);
    CreateTestCaseService.log
        .atInfo()
        .setMessage("test case created")
        .addKeyValue("question.id", testCase.questionId().value())
        .addKeyValue("test_case.id", testCase.id().value())
        .log();
    return testCase;
  }
}
