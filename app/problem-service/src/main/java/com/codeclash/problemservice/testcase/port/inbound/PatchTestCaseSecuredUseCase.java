package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;

public interface PatchTestCaseSecuredUseCase {

  TestCase patch(PatchTestCaseRequest request);
}
