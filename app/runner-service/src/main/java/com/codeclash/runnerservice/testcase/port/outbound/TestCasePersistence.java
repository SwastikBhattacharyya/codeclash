package com.codeclash.runnerservice.testcase.port.outbound;

import com.codeclash.runnerservice.testcase.model.Id;
import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import java.util.List;

public interface TestCasePersistence {

  void upsert(TestCase testCase);

  void deleteById(Id id);

  List<TestCase> findByQuestionId(QuestionId questionId);
}
