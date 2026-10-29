package com.codeclash.runnerservice.submission.adapter.outbound.tracing;

import com.codeclash.runnerservice.submission.model.ExhaustedJob;
import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.model.JudgeOutcome;
import com.codeclash.runnerservice.submission.model.TraceCarrier;
import com.codeclash.runnerservice.submission.port.outbound.SubmissionTracing;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.ReceiverContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerSubmissionTracing implements SubmissionTracing {

  private static final String JUDGE_OBSERVATION = "submission.judge";
  private static final String EXHAUST_OBSERVATION = "submission.exhaust";
  private static final String LANGUAGE_KEY = "language";
  private static final String OUTCOME_KEY = "outcome";
  private static final String STATE_KEY = "state";
  private static final String SUBMISSION_ID_KEY = "submission.id";
  private static final String ERROR_OUTCOME = "error";
  private static final String RELEASED_OUTCOME = "released";
  private static final String FINISHED_OUTCOME = "finished";
  private static final String NO_STATE = "none";

  private final ObservationRegistry observationRegistry;
  private final Tracer tracer;
  private final Propagator propagator;

  private static ReceiverContext<Map<String, String>> receiverContext(TraceCarrier traceCarrier) {
    ReceiverContext<Map<String, String>> context =
        new ReceiverContext<>((headers, key) -> headers.get(key));
    context.setCarrier(traceCarrier.headers());
    return context;
  }

  @Override
  public TraceCarrier capture() {
    Span span = this.tracer.currentSpan();
    if (span == null) return new TraceCarrier(Map.of());
    Map<String, String> headers = new HashMap<>();
    this.propagator.inject(span.context(), headers, Map::put);
    return new TraceCarrier(headers);
  }

  @Override
  public void judge(Job job, Supplier<JudgeOutcome> judgement) {
    Observation observation =
        Observation.createNotStarted(
                MicrometerSubmissionTracing.JUDGE_OBSERVATION,
                () -> MicrometerSubmissionTracing.receiverContext(job.traceCarrier()),
                this.observationRegistry)
            .lowCardinalityKeyValue(MicrometerSubmissionTracing.LANGUAGE_KEY, job.language().value())
            .lowCardinalityKeyValue(
                MicrometerSubmissionTracing.OUTCOME_KEY, MicrometerSubmissionTracing.ERROR_OUTCOME)
            .lowCardinalityKeyValue(
                MicrometerSubmissionTracing.STATE_KEY, MicrometerSubmissionTracing.NO_STATE)
            .highCardinalityKeyValue(
                MicrometerSubmissionTracing.SUBMISSION_ID_KEY, job.id().value().toString());
    observation.observe(
        () -> {
          JudgeOutcome outcome = judgement.get();
          if (outcome.released()) {
            observation.lowCardinalityKeyValue(
                MicrometerSubmissionTracing.OUTCOME_KEY,
                MicrometerSubmissionTracing.RELEASED_OUTCOME);
            return;
          }
          observation.lowCardinalityKeyValue(
              MicrometerSubmissionTracing.OUTCOME_KEY, MicrometerSubmissionTracing.FINISHED_OUTCOME);
          observation.lowCardinalityKeyValue(
              MicrometerSubmissionTracing.STATE_KEY, outcome.state().value());
        });
  }

  @Override
  public void exhaust(ExhaustedJob job, Runnable action) {
    Observation.createNotStarted(
            MicrometerSubmissionTracing.EXHAUST_OBSERVATION,
            () -> MicrometerSubmissionTracing.receiverContext(job.traceCarrier()),
            this.observationRegistry)
        .highCardinalityKeyValue(
            MicrometerSubmissionTracing.SUBMISSION_ID_KEY, job.id().value().toString())
        .observe(action);
  }
}
