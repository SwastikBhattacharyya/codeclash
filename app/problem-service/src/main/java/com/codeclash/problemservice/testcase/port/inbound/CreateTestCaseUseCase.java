package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;

public interface CreateTestCaseUseCase {

  TestCase create(CreateTestCaseRequest request);
}
