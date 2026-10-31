package com.codeclash.runnerservice.submission.adapter.inbound.worker;

import com.codeclash.runnerservice.submission.adapter.config.SubmissionProperties;
import com.codeclash.runnerservice.submission.port.inbound.JudgeNextSubmissionUseCase;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SubmissionWorkerPool implements SmartLifecycle {

  private static final int PHASE = Integer.MAX_VALUE - 1000;
  private static final long SHUTDOWN_WAIT_SECONDS = 30;
  private static final String ACTIVE_WORKERS_METRIC = "submission.workers.active";
  private static final String MAX_WORKERS_METRIC = "submission.workers.max";

  private final JudgeNextSubmissionUseCase judgeNextSubmissionUseCase;
  private final SubmissionProperties submissionProperties;
  private final WorkSignal workSignal;
  private final MeterRegistry meterRegistry;
  private ExecutorService workers;
  private Semaphore permits;
  private volatile boolean running;

  @Override
  public void start() {
    int maxConcurrent = this.submissionProperties.worker().maxConcurrent();
    this.permits = new Semaphore(maxConcurrent);
    this.workers = Executors.newVirtualThreadPerTaskExecutor();
    this.running = true;
    Gauge.builder(
            SubmissionWorkerPool.ACTIVE_WORKERS_METRIC,
            this,
            pool -> maxConcurrent - pool.permits.availablePermits())
        .register(this.meterRegistry);
    Gauge.builder(SubmissionWorkerPool.MAX_WORKERS_METRIC, () -> maxConcurrent)
        .register(this.meterRegistry);
    for (int i = 0; i < maxConcurrent; i++) this.workers.submit(this::work);
    SubmissionWorkerPool.log
        .atInfo()
        .setMessage("submission workers started")
        .addKeyValue("count", maxConcurrent)
        .log();
  }

  @Override
  public void stop() {
    this.running = false;
    this.workSignal.signal();
    this.workers.shutdown();
    try {
      if (!this.workers.awaitTermination(SubmissionWorkerPool.SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS))
        this.workers.shutdownNow();
    } catch (InterruptedException exception) {
      this.workers.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  @Override
  public int getPhase() {
    return SubmissionWorkerPool.PHASE;
  }

  private void work() {
    while (this.running) {
      try {
        this.permits.acquire();
        boolean processed;
        try {
          processed = this.judgeNextSubmissionUseCase.judgeNext();
        } finally {
          this.permits.release();
        }
        if (!processed) this.workSignal.await(this.submissionProperties.worker().pollInterval());
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return;
      } catch (RuntimeException exception) {
        SubmissionWorkerPool.log
            .atError()
            .setMessage("submission worker failed")
            .setCause(exception)
            .log();
        try {
          this.workSignal.await(this.submissionProperties.worker().pollInterval());
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }
}
