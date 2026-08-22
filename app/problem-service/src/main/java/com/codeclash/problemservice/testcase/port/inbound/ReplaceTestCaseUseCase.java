package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;

public interface ReplaceTestCaseUseCase {

  ReplaceTestCaseResult replace(ReplaceTestCaseRequest request);
}
