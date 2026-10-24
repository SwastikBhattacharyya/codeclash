package com.codeclash.runnerservice.testcase.port.inbound;

import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import java.util.List;

public interface FindTestCasesUseCase {

  List<TestCase> findByQuestionId(QuestionId questionId);
}
