package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.PatchTestCaseUseCase;
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
public class PatchTestCaseService implements PatchTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;
  private final TestCaseEventPublisher testCaseEventPublisher;

  @Override
  @Transactional
  public TestCase patch(PatchTestCaseRequest request) {
    TestCase testCase =
        this.testCasePersistence
            .patch(request)
            .orElseThrow(() -> new TestCaseNotFoundException(request.id().value()));
    this.testCaseEventPublisher.written(testCase);
    PatchTestCaseService.log
        .atInfo()
        .setMessage("test case patched")
        .addKeyValue("question.id", testCase.questionId().value())
        .addKeyValue("test_case.id", testCase.id().value())
        .log();
    return testCase;
  }
}
