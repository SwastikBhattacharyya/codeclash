package com.codeclash.problemservice.testcase.port.inbound;

import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;

public interface DeleteTestCaseSecuredUseCase {

  void delete(QuestionId questionId, Id id);
}
