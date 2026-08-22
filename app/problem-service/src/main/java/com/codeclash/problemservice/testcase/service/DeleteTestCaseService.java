package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import com.codeclash.problemservice.testcase.port.inbound.DeleteTestCaseUseCase;
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
public class DeleteTestCaseService implements DeleteTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;
  private final TestCaseEventPublisher testCaseEventPublisher;

  @Override
  @Transactional
  public void delete(QuestionId questionId, Id id) {
    TestCase testCase =
        this.testCasePersistence
            .deleteById(questionId, id)
            .orElseThrow(() -> new TestCaseNotFoundException(id.value()));
    this.testCaseEventPublisher.deleted(testCase);
    DeleteTestCaseService.log
        .atInfo()
        .setMessage("test case deleted")
        .addKeyValue("question.id", testCase.questionId().value())
        .addKeyValue("test_case.id", testCase.id().value())
        .log();
  }
}
