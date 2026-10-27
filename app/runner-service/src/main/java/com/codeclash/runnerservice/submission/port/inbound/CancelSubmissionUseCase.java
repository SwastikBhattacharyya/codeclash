package com.codeclash.runnerservice.submission.port.inbound;

import com.codeclash.runnerservice.submission.model.Id;

public interface CancelSubmissionUseCase {

  void cancel(Id id);
}
