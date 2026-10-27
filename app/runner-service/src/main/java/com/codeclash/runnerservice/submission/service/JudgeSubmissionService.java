package com.codeclash.runnerservice.submission.service;

import com.codeclash.runnerservice.submission.model.ActualOutput;
import com.codeclash.runnerservice.submission.model.Compilation;
import com.codeclash.runnerservice.submission.model.ExhaustedJob;
import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.model.JudgeOutcome;
import com.codeclash.runnerservice.submission.model.JudgeLimits;
import com.codeclash.runnerservice.submission.model.JudgeSettings;
import com.codeclash.runnerservice.submission.model.JudgeTestCase;
import com.codeclash.runnerservice.submission.model.SubmissionStateChange;
import com.codeclash.runnerservice.submission.model.TestRun;
import com.codeclash.runnerservice.submission.model.exception.SandboxFailureException;
import com.codeclash.runnerservice.submission.port.inbound.JudgeNextSubmissionUseCase;
import com.codeclash.runnerservice.submission.port.outbound.JobPersistence;
import com.codeclash.runnerservice.submission.port.outbound.JudgeMetrics;
import com.codeclash.runnerservice.submission.port.outbound.QuestionLimitsLookup;
import com.codeclash.runnerservice.submission.port.outbound.SandboxProvider;
import com.codeclash.runnerservice.submission.port.outbound.SandboxSession;
import com.codeclash.runnerservice.submission.port.outbound.SubmissionStateEventPublisher;
import com.codeclash.runnerservice.submission.port.outbound.SubmissionTracing;
import com.codeclash.runnerservice.submission.port.outbound.TestCaseLookup;
import com.codeclash.contracts.problem.submission.SubmissionState;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class JudgeSubmissionService implements JudgeNextSubmissionUseCase {

  private final JobPersistence jobPersistence;
  private final QuestionLimitsLookup questionLimitsLookup;
  private final TestCaseLookup testCaseLookup;
  private final SandboxProvider sandboxProvider;
  private final SubmissionStateEventPublisher submissionStateEventPublisher;
  private final SubmissionTracing submissionTracing;
  private final JudgeMetrics judgeMetrics;
  private final TransactionTemplate transactionTemplate;
  private final JudgeSettings judgeSettings;

  private static SubmissionState toState(TestRun run) {
    return switch (run.verdict()) {
      case OK -> SubmissionState.CORRECT;
      case TIME_LIMIT_EXCEEDED -> SubmissionState.TIME_LIMIT_EXCEEDED;
      case MEMORY_LIMIT_EXCEEDED -> SubmissionState.MEMORY_LIMIT_EXCEEDED;
      case RUNTIME_ERROR -> SubmissionState.RUNTIME_ERROR;
    };
  }

  private static ActualOutput actualOutput(
      SubmissionState state, JudgeTestCase testCase, TestRun run) {
    if (!testCase.isPublic() || run.output() == null) return null;
    if (state != SubmissionState.INCORRECT && state != SubmissionState.RUNTIME_ERROR) return null;
    return ActualOutput.of(run.output());
  }

  @Override
  public boolean judgeNext() {
    this.failExhausted();
    Optional<Job> claimed =
        this.jobPersistence.claimNext(this.judgeSettings.lease(), this.judgeSettings.maxAttempts());
    if (claimed.isEmpty()) return false;
    Job job = claimed.get();
    if (job.attempts() == 1) this.judgeMetrics.queued(job);
    this.submissionTracing.judge(job, () -> this.judgeClaimed(job));
    return true;
  }

  private JudgeOutcome judgeClaimed(Job job) {
    SubmissionStateChange result;
    try {
      result = this.judge(job);
    } catch (SandboxFailureException exception) {
      JudgeSubmissionService.log
          .atWarn()
          .setMessage("sandbox failed")
          .addKeyValue("submission.id", job.id().value())
          .addKeyValue("attempt", job.attempts())
          .addKeyValue("error.message", exception.getMessage())
          .log();
      if (job.attempts() < this.judgeSettings.maxAttempts()) {
        this.jobPersistence.release(job.id());
        JudgeSubmissionService.log
            .atInfo()
            .setMessage("submission released for retry")
            .addKeyValue("submission.id", job.id().value())
            .addKeyValue("attempt", job.attempts())
            .log();
        return JudgeOutcome.retry();
      }
      result = SubmissionStateChange.of(job.id(), SubmissionState.RUNNER_ERROR);
    }
    if (!this.finish(result)) {
      JudgeSubmissionService.log
          .atWarn()
          .setMessage("submission result discarded")
          .addKeyValue("submission.id", job.id().value())
          .addKeyValue("state", result.state().value())
          .addKeyValue("attempt", job.attempts())
          .log();
      return JudgeOutcome.finished(result.state());
    }
    JudgeSubmissionService.log
        .atInfo()
        .setMessage("submission judged")
        .addKeyValue("submission.id", job.id().value())
        .addKeyValue("state", result.state().value())
        .addKeyValue("attempt", job.attempts())
        .log();
    return JudgeOutcome.finished(result.state());
  }

  private void failExhausted() {
    this.transactionTemplate.executeWithoutResult(
        status ->
            this.jobPersistence
                .completeExhausted(this.judgeSettings.lease(), this.judgeSettings.maxAttempts())
                .forEach(
                    job ->
                        this.submissionTracing.exhaust(job, () -> this.publishExhausted(job))));
  }

  private void publishExhausted(ExhaustedJob job) {
    this.submissionStateEventPublisher.publish(
        SubmissionStateChange.of(job.id(), SubmissionState.RUNNER_ERROR));
    JudgeSubmissionService.log
        .atWarn()
        .setMessage("submission attempts exhausted")
        .addKeyValue("submission.id", job.id().value())
        .log();
  }

  private SubmissionStateChange judge(Job job) {
    Optional<JudgeLimits> limits = this.questionLimitsLookup.find(job.questionId());
    List<JudgeTestCase> testCases = this.testCaseLookup.findByQuestionId(job.questionId());
    if (limits.isEmpty() || testCases.isEmpty())
      return SubmissionStateChange.of(job.id(), SubmissionState.RUNNER_ERROR);
    try (SandboxSession sandbox = this.sandboxProvider.open()) {
      sandbox.writeSource(job.language(), job.code());
      Compilation compilation = sandbox.compile(job.language());
      if (!compilation.compiled())
        return SubmissionStateChange.compilationError(job.id(), compilation.output());
      this.transactionTemplate.executeWithoutResult(
          status ->
              this.submissionStateEventPublisher.publish(
                  SubmissionStateChange.of(job.id(), SubmissionState.COMPILED)));
      sandbox.limitMemory(limits.get().maxRamLimitMb());
      int timeLimitMs =
          this.judgeSettings.runTimeLimitMs(job.language(), limits.get().maxRunTimeMs());
      long maxTimeMs = 0;
      long maxMemoryKb = 0;
      for (JudgeTestCase testCase : testCases) {
        TestRun run = sandbox.run(job.language(), timeLimitMs, testCase.input());
        SubmissionState state = JudgeSubmissionService.toState(run);
        if (state == SubmissionState.CORRECT && !testCase.expectedOutput().matches(run.output()))
          state = SubmissionState.INCORRECT;
        if (state != SubmissionState.CORRECT)
          return SubmissionStateChange.failed(
              job.id(), state, testCase.id(), JudgeSubmissionService.actualOutput(state, testCase, run));
        maxTimeMs = Math.max(maxTimeMs, run.timeMs());
        maxMemoryKb = Math.max(maxMemoryKb, run.memoryKb());
      }
      return SubmissionStateChange.correct(job.id(), maxTimeMs, maxMemoryKb);
    }
  }

  private boolean finish(SubmissionStateChange result) {
    return Boolean.TRUE.equals(
        this.transactionTemplate.execute(
            status -> {
              if (!this.jobPersistence.complete(result.id(), result.state())) return false;
              this.submissionStateEventPublisher.publish(result);
              return true;
            }));
  }
}
