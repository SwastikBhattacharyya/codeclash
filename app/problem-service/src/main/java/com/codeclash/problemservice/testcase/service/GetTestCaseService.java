package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import com.codeclash.problemservice.testcase.port.inbound.GetTestCaseUseCase;
import com.codeclash.problemservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetTestCaseService implements GetTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;

  @Override
  public TestCase findById(QuestionId questionId, Id id) {
    return this.testCasePersistence
        .findById(questionId, id)
        .orElseThrow(() -> new TestCaseNotFoundException(id.value()));
  }
}
