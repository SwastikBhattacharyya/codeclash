package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.ExhaustedJob;
import com.codeclash.runnerservice.submission.model.Id;
import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.model.JobCount;
import com.codeclash.runnerservice.submission.model.TraceCarrier;
import com.codeclash.runnerservice.submission.model.request.EnqueueSubmissionRequest;
import com.codeclash.contracts.problem.submission.SubmissionState;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface JobPersistence {

  boolean insert(EnqueueSubmissionRequest request, TraceCarrier traceCarrier);

  boolean deleteIfNotRunning(Id id);

  List<ExhaustedJob> completeExhausted(Duration lease, int maxAttempts);

  Optional<Job> claimNext(Duration lease, int maxAttempts);

  List<JobCount> countByStatus();

  void release(Id id);

  boolean complete(Id id, SubmissionState result);
}
