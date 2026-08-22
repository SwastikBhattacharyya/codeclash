package com.codeclash.problemservice.testcase.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;
import java.util.List;
import java.util.Optional;

public interface TestCasePersistence {

  PagedEntity<TestCase> query(QueryTestCaseRequest request);

  Optional<TestCase> findById(QuestionId questionId, Id id);

  List<Id> findPublicIds(List<Id> ids);

  TestCase create(CreateTestCaseRequest request);

  Optional<ReplaceTestCaseResult> replace(ReplaceTestCaseRequest request);

  Optional<TestCase> patch(PatchTestCaseRequest request);

  Optional<TestCase> deleteById(QuestionId questionId, Id id);
}
