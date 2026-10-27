package com.codeclash.runnerservice.submission.service;

import com.codeclash.runnerservice.submission.model.request.EnqueueSubmissionRequest;
import com.codeclash.runnerservice.submission.port.inbound.EnqueueSubmissionUseCase;
import com.codeclash.runnerservice.submission.port.outbound.JobPersistence;
import com.codeclash.runnerservice.submission.port.outbound.SubmissionTracing;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class EnqueueSubmissionService implements EnqueueSubmissionUseCase {

  private final JobPersistence jobPersistence;
  private final SubmissionTracing submissionTracing;

  @Override
  public void enqueue(EnqueueSubmissionRequest request) {
    if (!this.jobPersistence.insert(request, this.submissionTracing.capture())) {
      EnqueueSubmissionService.log
          .atDebug()
          .setMessage("submission enqueue skipped")
          .addKeyValue("submission.id", request.id().value())
          .log();
      return;
    }
    EnqueueSubmissionService.log
        .atInfo()
        .setMessage("submission enqueued")
        .addKeyValue("submission.id", request.id().value())
        .addKeyValue("language", request.language().value())
        .log();
  }
}
