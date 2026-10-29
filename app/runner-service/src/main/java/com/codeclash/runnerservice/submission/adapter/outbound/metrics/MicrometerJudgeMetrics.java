package com.codeclash.runnerservice.submission.adapter.outbound.metrics;

import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.port.outbound.JudgeMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerJudgeMetrics implements JudgeMetrics {

  private static final String QUEUE_WAIT_METRIC = "submission.queue.wait";
  private static final String LANGUAGE_KEY = "language";

  private final MeterRegistry meterRegistry;

  @Override
  public void queued(Job job) {
    Timer.builder(MicrometerJudgeMetrics.QUEUE_WAIT_METRIC)
        .tag(MicrometerJudgeMetrics.LANGUAGE_KEY, job.language().value())
        .register(this.meterRegistry)
        .record(job.queuedFor());
  }
}
