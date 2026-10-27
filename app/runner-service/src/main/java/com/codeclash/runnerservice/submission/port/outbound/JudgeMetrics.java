package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.Job;

public interface JudgeMetrics {

  void queued(Job job);
}
