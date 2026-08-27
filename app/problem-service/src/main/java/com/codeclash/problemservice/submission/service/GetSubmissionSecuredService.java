package com.codeclash.problemservice.submission.service;

import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.port.inbound.GetSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.GetSubmissionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetSubmissionSecuredService implements GetSubmissionSecuredUseCase {

  private final GetSubmissionUseCase getSubmissionUseCase;
  private final SubmissionQuestionGuard submissionQuestionGuard;
  private final SubmissionOutputGuard submissionOutputGuard;

  @Override
  public Submission findById(Id id) {
    Submission submission = this.getSubmissionUseCase.findById(id);
    this.submissionQuestionGuard.requireQuestionReadable(submission.questionId());
    return this.submissionOutputGuard.reveal(submission);
  }
}
