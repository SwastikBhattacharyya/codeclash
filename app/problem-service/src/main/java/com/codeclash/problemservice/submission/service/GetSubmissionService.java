package com.codeclash.problemservice.submission.service;

import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.exception.SubmissionNotFoundException;
import com.codeclash.problemservice.submission.port.inbound.GetSubmissionUseCase;
import com.codeclash.problemservice.submission.port.outbound.SubmissionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetSubmissionService implements GetSubmissionUseCase {

  private final SubmissionPersistence submissionPersistence;

  @Override
  public Submission findById(Id id) {
    return this.submissionPersistence
        .findById(id)
        .orElseThrow(() -> new SubmissionNotFoundException(id.value()));
  }
}
