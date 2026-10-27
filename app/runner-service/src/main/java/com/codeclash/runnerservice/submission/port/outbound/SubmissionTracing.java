package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.ExhaustedJob;
import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.model.JudgeOutcome;
import com.codeclash.runnerservice.submission.model.TraceCarrier;
import java.util.function.Supplier;

public interface SubmissionTracing {

  TraceCarrier capture();

  void judge(Job job, Supplier<JudgeOutcome> judgement);

  void exhaust(ExhaustedJob job, Runnable action);
}
