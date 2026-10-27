package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.SubmissionStateChange;

public interface SubmissionStateEventPublisher {

  void publish(SubmissionStateChange change);
}
