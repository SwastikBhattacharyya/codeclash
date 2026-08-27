package com.codeclash.problemservice.submission.port.outbound;

import com.codeclash.problemservice.submission.model.Submission;

public interface SubmissionEventPublisher {

  void written(Submission submission);

  void deleted(Submission submission);
}
