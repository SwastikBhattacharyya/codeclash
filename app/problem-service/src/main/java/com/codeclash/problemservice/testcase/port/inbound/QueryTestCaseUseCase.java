package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;

public interface QueryTestCaseUseCase {

  PagedEntity<TestCase> query(QueryTestCaseRequest request);
}
