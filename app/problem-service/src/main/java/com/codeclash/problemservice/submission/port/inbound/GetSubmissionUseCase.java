package com.codeclash.problemservice.submission.port.inbound;

import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;

public interface GetSubmissionUseCase {

  Submission findById(Id id);
}
