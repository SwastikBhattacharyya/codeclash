package com.codeclash.contestservice.submission.service;

import com.codeclash.contestservice.submission.model.Id;
import com.codeclash.contestservice.submission.model.Submission;
import com.codeclash.contestservice.submission.model.result.SyncSubmissionResult;
import com.codeclash.contestservice.submission.port.inbound.SyncSubmissionUseCase;
import com.codeclash.contestservice.submission.port.outbound.LeaderboardRebuilder;
import com.codeclash.contestservice.submission.port.outbound.SubmissionPersistence;
import com.codeclash.contracts.problem.submission.SubmissionState;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncSubmissionService implements SyncSubmissionUseCase {

  private final SubmissionPersistence submissionPersistence;
  private final LeaderboardRebuilder leaderboardRebuilder;

  @Override
  @Transactional
  public void write(Submission submission) {
    Optional<SyncSubmissionResult> result = this.submissionPersistence.upsert(submission);
    if (result.isEmpty()) return;
    if (result.get().scoreChanged()) this.leaderboardRebuilder.rebuildFor(submission);
    SyncSubmissionService.log
        .atDebug()
        .setMessage("submission synced")
        .addKeyValue("submission.id", submission.id().value())
        .addKeyValue("state", submission.state().value())
        .log();
  }

  @Override
  @Transactional
  public void delete(Id id) {
    this.submissionPersistence
        .deleteById(id)
        .filter(submission -> submission.state() == SubmissionState.CORRECT)
        .ifPresent(this.leaderboardRebuilder::rebuildFor);
    SyncSubmissionService.log
        .atDebug()
        .setMessage("submission removed")
        .addKeyValue("submission.id", id.value())
        .log();
  }
}
