package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;

public interface GetTestCaseUseCase {

  TestCase findById(QuestionId questionId, Id id);
}
