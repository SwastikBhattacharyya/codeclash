package com.codeclash.runnerservice.testcase.port.inbound;

import com.codeclash.runnerservice.testcase.model.Id;
import com.codeclash.runnerservice.testcase.model.TestCase;

public interface SyncTestCaseUseCase {

  void write(TestCase testCase);

  void delete(Id id);
}
