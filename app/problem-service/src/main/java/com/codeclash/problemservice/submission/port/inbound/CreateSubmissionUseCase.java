package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.owner.Owned;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;

public interface CreateSubmissionUseCase {

  Submission create(CreateSubmissionRequest<Owned> request);
}
