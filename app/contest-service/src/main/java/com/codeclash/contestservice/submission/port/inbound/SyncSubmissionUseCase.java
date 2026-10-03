package com.codeclash.contestservice.submission.port.inbound;

import com.codeclash.contestservice.submission.model.Id;
import com.codeclash.contestservice.submission.model.Submission;

public interface SyncSubmissionUseCase {

  void write(Submission submission);

  void delete(Id id);
}
