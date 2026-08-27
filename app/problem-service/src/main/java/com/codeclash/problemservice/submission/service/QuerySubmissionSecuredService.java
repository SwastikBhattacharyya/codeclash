package com.codeclash.problemservice.submission.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;
import com.codeclash.problemservice.submission.port.inbound.QuerySubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.QuerySubmissionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QuerySubmissionSecuredService implements QuerySubmissionSecuredUseCase {

  private final QuerySubmissionUseCase querySubmissionUseCase;
  private final SubmissionQuestionGuard submissionQuestionGuard;
  private final SubmissionOutputGuard submissionOutputGuard;

  @Override
  public PagedEntity<Submission> query(QuerySubmissionRequest request) {
    QuerySubmissionRequest scoped =
        this.submissionQuestionGuard.canReadPrivate()
            ? request
            : request.toBuilder().questionVisible(true).build();
    return this.submissionOutputGuard.reveal(this.querySubmissionUseCase.query(scoped));
  }
}
