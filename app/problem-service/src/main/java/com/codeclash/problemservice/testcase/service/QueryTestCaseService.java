package com.codeclash.problemservice.testcase.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.QueryTestCaseUseCase;
import com.codeclash.problemservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryTestCaseService implements QueryTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<TestCase> query(QueryTestCaseRequest request) {
    return this.testCasePersistence.query(request);
  }
}
