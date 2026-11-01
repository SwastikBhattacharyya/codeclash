package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.problemservice.submission.model.request.ApplySubmissionStateRequest;

public interface ApplySubmissionStateUseCase {

  void apply(ApplySubmissionStateRequest request);
}
