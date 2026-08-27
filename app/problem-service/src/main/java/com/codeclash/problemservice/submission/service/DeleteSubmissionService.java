package com.codeclash.problemservice.submission.service;

import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.exception.SubmissionNotFoundException;
import com.codeclash.problemservice.submission.port.inbound.DeleteSubmissionUseCase;
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
public class DeleteSubmissionService implements DeleteSubmissionUseCase {

  private final SubmissionPersistence submissionPersistence;
  private final SubmissionEventPublisher submissionEventPublisher;

  @Override
  @Transactional
  public void delete(Id id) {
    Submission submission =
        this.submissionPersistence
            .deleteById(id)
            .orElseThrow(() -> new SubmissionNotFoundException(id.value()));
    this.submissionEventPublisher.deleted(submission);
    DeleteSubmissionService.log
        .atInfo()
        .setMessage("submission deleted")
        .addKeyValue("submission.id", submission.id().value())
        .log();
  }
}
