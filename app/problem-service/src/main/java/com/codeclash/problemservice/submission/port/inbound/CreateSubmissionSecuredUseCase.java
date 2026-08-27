package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.owner.Unowned;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;

public interface CreateSubmissionSecuredUseCase {

  Submission create(CreateSubmissionRequest<Unowned> request);
}
