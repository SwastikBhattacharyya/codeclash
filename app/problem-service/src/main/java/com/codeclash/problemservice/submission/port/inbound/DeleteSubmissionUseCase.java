package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.problemservice.submission.model.Id;

public interface DeleteSubmissionUseCase {

  void delete(Id id);
}
