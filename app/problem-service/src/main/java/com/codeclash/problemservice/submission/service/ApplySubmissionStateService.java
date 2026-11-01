package com.codeclash.problemservice.submission.service;

import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.request.ApplySubmissionStateRequest;
import com.codeclash.problemservice.submission.port.inbound.ApplySubmissionStateUseCase;
import com.codeclash.problemservice.submission.port.outbound.SubmissionEventPublisher;
import com.codeclash.problemservice.submission.port.outbound.SubmissionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ApplySubmissionStateService implements ApplySubmissionStateUseCase {

  private static final List<SubmissionState> PENDING =
      List.of(SubmissionState.QUEUED, SubmissionState.COMPILED);

  private static final Map<SubmissionState, List<SubmissionState>> PREDECESSORS =
      Map.of(
          SubmissionState.COMPILED, List.of(SubmissionState.QUEUED),
          SubmissionState.COMPILATION_ERROR, List.of(SubmissionState.QUEUED),
          SubmissionState.CORRECT, ApplySubmissionStateService.PENDING,
          SubmissionState.INCORRECT, ApplySubmissionStateService.PENDING,
          SubmissionState.RUNTIME_ERROR, ApplySubmissionStateService.PENDING,
          SubmissionState.TIME_LIMIT_EXCEEDED, ApplySubmissionStateService.PENDING,
          SubmissionState.MEMORY_LIMIT_EXCEEDED, ApplySubmissionStateService.PENDING,
          SubmissionState.RUNNER_ERROR, ApplySubmissionStateService.PENDING);

  private final SubmissionPersistence submissionPersistence;
  private final SubmissionEventPublisher submissionEventPublisher;

  @Override
  @Transactional
  public void apply(ApplySubmissionStateRequest request) {
    List<SubmissionState> predecessors =
        ApplySubmissionStateService.PREDECESSORS.get(request.state());
    if (predecessors == null) return;
    Optional<Submission> submission = this.submissionPersistence.applyState(request, predecessors);
    if (submission.isEmpty()) {
      ApplySubmissionStateService.log
          .atDebug()
          .setMessage("submission state skipped")
          .addKeyValue("submission.id", request.id().value())
          .addKeyValue("state", request.state().value())
          .log();
      return;
    }
    this.submissionEventPublisher.written(submission.get());
    ApplySubmissionStateService.log
        .atInfo()
        .setMessage("submission state applied")
        .addKeyValue("submission.id", request.id().value())
        .addKeyValue("state", request.state().value())
        .log();
  }
}
