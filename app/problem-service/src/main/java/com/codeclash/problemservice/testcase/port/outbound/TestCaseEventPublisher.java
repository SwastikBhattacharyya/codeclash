package com.codeclash.problemservice.testcase.port.outbound;

import com.codeclash.problemservice.testcase.model.TestCase;

public interface TestCaseEventPublisher {

  void written(TestCase testCase);

  void deleted(TestCase testCase);
}
