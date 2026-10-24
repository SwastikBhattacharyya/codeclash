package com.codeclash.runnerservice.testcase.service;

import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import com.codeclash.runnerservice.testcase.port.inbound.FindTestCasesUseCase;
import com.codeclash.runnerservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class FindTestCasesService implements FindTestCasesUseCase {

  private final TestCasePersistence testCasePersistence;

  @Override
  public List<TestCase> findByQuestionId(QuestionId questionId) {
    return this.testCasePersistence.findByQuestionId(questionId);
  }
}
