package com.codeclash.runnerservice.submission.service;

import com.codeclash.runnerservice.submission.model.Id;
import com.codeclash.runnerservice.submission.port.inbound.CancelSubmissionUseCase;
import com.codeclash.runnerservice.submission.port.outbound.JobPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CancelSubmissionService implements CancelSubmissionUseCase {

  private final JobPersistence jobPersistence;

  @Override
  public void cancel(Id id) {
    if (!this.jobPersistence.deleteIfNotRunning(id)) {
      CancelSubmissionService.log
          .atDebug()
          .setMessage("submission cancel skipped")
          .addKeyValue("submission.id", id.value())
          .log();
      return;
    }
    CancelSubmissionService.log
        .atInfo()
        .setMessage("submission cancelled")
        .addKeyValue("submission.id", id.value())
        .log();
  }
}
