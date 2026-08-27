package com.codeclash.problemservice.submission.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;
import com.codeclash.problemservice.submission.port.inbound.QuerySubmissionUseCase;
import com.codeclash.problemservice.submission.port.outbound.SubmissionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QuerySubmissionService implements QuerySubmissionUseCase {

  private final SubmissionPersistence submissionPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Submission> query(QuerySubmissionRequest request) {
    return this.submissionPersistence.query(request);
  }
}
