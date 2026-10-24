package com.codeclash.runnerservice.testcase.api.usecase;

import com.codeclash.runnerservice.testcase.api.TestCaseView;
import java.util.List;
import java.util.UUID;

public interface FindTestCasesByQuestionApiUseCase {

  List<TestCaseView> findByQuestionId(UUID questionId);
}
