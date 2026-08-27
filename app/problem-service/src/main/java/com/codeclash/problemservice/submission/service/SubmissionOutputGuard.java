package com.codeclash.problemservice.submission.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.port.outbound.TestCaseLookup;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SubmissionOutputGuard {

  private final CurrentSession currentSession;
  private final TestCaseLookup testCaseLookup;

  private static Submission mask(Submission submission, Set<FailedTestCaseId> publicIds) {
    if (submission.actualOutput() == null) return submission;
    if (submission.failedTestCaseId() != null && publicIds.contains(submission.failedTestCaseId()))
      return submission;
    return submission.withActualOutput(null);
  }

  Submission reveal(Submission submission) {
    if (this.canReadPrivate()) return submission;
    return SubmissionOutputGuard.mask(submission, this.publicIds(List.of(submission)));
  }

  PagedEntity<Submission> reveal(PagedEntity<Submission> page) {
    if (this.canReadPrivate()) return page;
    Set<FailedTestCaseId> publicIds = this.publicIds(page.data());
    return page.map(submission -> SubmissionOutputGuard.mask(submission, publicIds));
  }

  private boolean canReadPrivate() {
    return this.currentSession.hasPermission(ProblemPermissions.TEST_CASE_READ_IS_PRIVATE);
  }

  private Set<FailedTestCaseId> publicIds(List<Submission> submissions) {
    List<FailedTestCaseId> ids =
        submissions.stream()
            .filter(submission -> submission.actualOutput() != null)
            .map(Submission::failedTestCaseId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    if (ids.isEmpty()) return Set.of();
    return this.testCaseLookup.findPublic(ids);
  }
}
