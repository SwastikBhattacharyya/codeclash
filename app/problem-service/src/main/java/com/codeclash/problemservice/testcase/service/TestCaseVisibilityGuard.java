package com.codeclash.problemservice.testcase.service;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.port.inbound.GetTestCaseUseCase;
import com.codeclash.problemservice.testcase.port.outbound.QuestionLookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TestCaseVisibilityGuard {

  private final CurrentSession currentSession;
  private final QuestionLookup questionLookup;
  private final GetTestCaseUseCase getTestCaseUseCase;

  QueryTestCaseRequest visibleFilter(QueryTestCaseRequest request) {
    List<String> missing = this.missingForQuestion(request.questionId());
    QueryTestCaseRequest filtered = request;
    if (!this.canReadPrivateTestCase()) {
      if (request.isPublic() == null) filtered = request.toBuilder().isPublic(true).build();
      else if (!request.isPublic()) missing.add(ProblemPermissions.TEST_CASE_READ_IS_PRIVATE);
    }
    TestCaseVisibilityGuard.throwIfMissing(missing);
    return filtered;
  }

  void requireQuestionVisible(QuestionId questionId) {
    TestCaseVisibilityGuard.throwIfMissing(this.missingForQuestion(questionId));
  }

  void requireVisible(TestCase testCase) {
    if (!testCase.isPublic().value() && !this.canReadPrivateTestCase())
      throw new ForbiddenException(List.of(ProblemPermissions.TEST_CASE_READ_IS_PRIVATE));
  }

  void requireCreatable(QuestionId questionId, Public isPublic) {
    List<String> missing = this.missingForQuestion(questionId);
    if (!isPublic.value() && !this.canWritePrivateTestCase())
      missing.add(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE);
    TestCaseVisibilityGuard.throwIfMissing(missing);
  }

  void requireReplaceable(QuestionId questionId, Id id, Public isPublic) {
    List<String> missing = this.missingForQuestion(questionId);
    if (!this.canWritePrivateTestCase()
        && (!isPublic.value()
            || (missing.isEmpty()
                && this.findStored(questionId, id).map(this::isPrivate).orElse(false))))
      missing.add(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE);
    TestCaseVisibilityGuard.throwIfMissing(missing);
  }

  void requirePatchable(QuestionId questionId, Id id, PatchObject<Public> isPublic) {
    List<String> missing = this.missingForQuestion(questionId);
    if (!this.canWritePrivateTestCase()
        && ((isPublic.isPresent() && !isPublic.orElse(null).value())
            || (missing.isEmpty()
                && this.isPrivate(this.getTestCaseUseCase.findById(questionId, id)))))
      missing.add(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE);
    TestCaseVisibilityGuard.throwIfMissing(missing);
  }

  void requireDeletable(QuestionId questionId, Id id) {
    List<String> missing = this.missingForQuestion(questionId);
    if (!this.canWritePrivateTestCase()
        && missing.isEmpty()
        && this.isPrivate(this.getTestCaseUseCase.findById(questionId, id)))
      missing.add(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE);
    TestCaseVisibilityGuard.throwIfMissing(missing);
  }

  private List<String> missingForQuestion(QuestionId questionId) {
    List<String> missing = new ArrayList<>();
    if (!this.currentSession.hasPermission(ProblemPermissions.QUESTION_READ_IS_PRIVATE)
        && !this.questionLookup.findById(questionId).visible())
      missing.add(ProblemPermissions.QUESTION_READ_IS_PRIVATE);
    return missing;
  }

  private boolean canReadPrivateTestCase() {
    return this.currentSession.hasPermission(ProblemPermissions.TEST_CASE_READ_IS_PRIVATE);
  }

  private boolean canWritePrivateTestCase() {
    return this.currentSession.hasPermission(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE);
  }

  private boolean isPrivate(TestCase testCase) {
    return !testCase.isPublic().value();
  }

  private Optional<TestCase> findStored(QuestionId questionId, Id id) {
    try {
      return Optional.of(this.getTestCaseUseCase.findById(questionId, id));
    } catch (TestCaseNotFoundException exception) {
      return Optional.empty();
    }
  }

  private static void throwIfMissing(List<String> missing) {
    if (!missing.isEmpty()) throw new ForbiddenException(missing);
  }
}
