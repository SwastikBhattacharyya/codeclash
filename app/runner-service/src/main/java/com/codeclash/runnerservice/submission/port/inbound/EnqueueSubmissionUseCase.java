package com.codeclash.runnerservice.submission.port.inbound;

import com.codeclash.runnerservice.submission.model.request.EnqueueSubmissionRequest;

public interface EnqueueSubmissionUseCase {

  void enqueue(EnqueueSubmissionRequest request);
}
