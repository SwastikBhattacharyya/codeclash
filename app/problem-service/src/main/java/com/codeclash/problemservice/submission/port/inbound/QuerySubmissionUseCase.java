package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;

public interface QuerySubmissionUseCase {

  PagedEntity<Submission> query(QuerySubmissionRequest request);
}
