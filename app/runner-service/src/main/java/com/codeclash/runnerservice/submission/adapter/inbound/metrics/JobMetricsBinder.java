package com.codeclash.runnerservice.submission.adapter.inbound.metrics;

import com.codeclash.runnerservice.submission.model.JobCount;
import com.codeclash.runnerservice.submission.model.JobStatus;
import com.codeclash.runnerservice.submission.port.inbound.CountJobsUseCase;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JobMetricsBinder implements MeterBinder {

  private static final String JOBS_METRIC = "submission.jobs";
  private static final String STATUS_KEY = "status";
  private static final Duration REFRESH_INTERVAL = Duration.ofSeconds(10);
  private static final List<JobStatus> STATUSES = List.of(JobStatus.QUEUED, JobStatus.RUNNING);

  private final CountJobsUseCase countJobsUseCase;
  private Map<JobStatus, Long> counts = Map.of();
  private Instant refreshedAt = Instant.EPOCH;

  @Override
  public void bindTo(MeterRegistry registry) {
    for (JobStatus status : JobMetricsBinder.STATUSES)
      Gauge.builder(JobMetricsBinder.JOBS_METRIC, this, binder -> binder.count(status))
          .tag(JobMetricsBinder.STATUS_KEY, status.value())
          .register(registry);
  }

  private synchronized double count(JobStatus status) {
    Instant now = Instant.now();
    if (now.isAfter(this.refreshedAt.plus(JobMetricsBinder.REFRESH_INTERVAL))) {
      this.refreshedAt = now;
      this.counts =
          this.countJobsUseCase.count().stream()
              .collect(Collectors.toMap(JobCount::status, JobCount::count));
    }
    return this.counts.getOrDefault(status, 0L);
  }
}
