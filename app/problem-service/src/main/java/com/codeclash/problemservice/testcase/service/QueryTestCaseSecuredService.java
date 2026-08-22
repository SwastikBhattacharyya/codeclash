package com.codeclash.problemservice.testcase.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.QueryTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.QueryTestCaseUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QueryTestCaseSecuredService implements QueryTestCaseSecuredUseCase {

  private final QueryTestCaseUseCase queryTestCaseUseCase;
  private final TestCaseVisibilityGuard testCaseVisibilityGuard;

  @Override
  public PagedEntity<TestCase> query(QueryTestCaseRequest request) {
    return this.queryTestCaseUseCase.query(this.testCaseVisibilityGuard.visibleFilter(request));
  }
}
