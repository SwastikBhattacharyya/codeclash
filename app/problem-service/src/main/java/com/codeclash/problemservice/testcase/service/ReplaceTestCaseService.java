package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;
import com.codeclash.problemservice.testcase.port.inbound.ReplaceTestCaseUseCase;
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
public class ReplaceTestCaseService implements ReplaceTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;
  private final TestCaseEventPublisher testCaseEventPublisher;

  @Override
  @Transactional
  public ReplaceTestCaseResult replace(ReplaceTestCaseRequest request) {
    ReplaceTestCaseResult result =
        this.testCasePersistence
            .replace(request)
            .orElseThrow(() -> new TestCaseNotFoundException(request.id().value()));
    this.testCaseEventPublisher.written(result.testCase());
    ReplaceTestCaseService.log
        .atInfo()
        .setMessage("test case replaced")
        .addKeyValue("question.id", result.testCase().questionId().value())
        .addKeyValue("test_case.id", result.testCase().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}
