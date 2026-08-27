package com.codeclash.problemservice.submission.service;

import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.owner.Owned;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;
import com.codeclash.problemservice.submission.port.inbound.CreateSubmissionUseCase;
import com.codeclash.problemservice.submission.port.outbound.SubmissionEventPublisher;
import com.codeclash.problemservice.submission.port.outbound.SubmissionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateSubmissionService implements CreateSubmissionUseCase {

  private final SubmissionPersistence submissionPersistence;
  private final SubmissionEventPublisher submissionEventPublisher;

  @Override
  @Transactional
  public Submission create(CreateSubmissionRequest<Owned> request) {
    Submission submission = this.submissionPersistence.create(request, SubmissionState.QUEUED);
    this.submissionEventPublisher.written(submission);
    CreateSubmissionService.log
        .atInfo()
        .setMessage("submission created")
        .addKeyValue("submission.id", submission.id().value())
        .addKeyValue("question.id", submission.questionId().value())
        .addKeyValue("language", submission.language().value())
        .log();
    return submission;
  }
}
