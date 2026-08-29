package com.codeclash.problemservice.submission.adapter.outbound.testcase;

import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import com.codeclash.problemservice.submission.port.outbound.TestCaseLookup;
import com.codeclash.problemservice.testcase.api.usecase.FindPublicTestCaseIdsApiUseCase;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionTestCaseApiAdapter implements TestCaseLookup {

  private final FindPublicTestCaseIdsApiUseCase findPublicTestCaseIdsApiUseCase;

  @Override
  public Set<FailedTestCaseId> findPublic(Collection<FailedTestCaseId> ids) {
    return this.findPublicTestCaseIdsApiUseCase
        .findPublicIds(ids.stream().map(FailedTestCaseId::value).toList())
        .stream()
        .map(FailedTestCaseId::unsafe)
        .collect(Collectors.toUnmodifiableSet());
  }
}
