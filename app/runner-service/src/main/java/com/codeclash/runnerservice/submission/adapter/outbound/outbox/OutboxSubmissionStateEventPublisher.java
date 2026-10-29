package com.codeclash.runnerservice.submission.adapter.outbound.outbox;

import com.codeclash.runnerservice.submission.adapter.config.SubmissionProperties;
import com.codeclash.runnerservice.submission.model.SubmissionStateChange;
import com.codeclash.runnerservice.submission.port.outbound.SubmissionStateEventPublisher;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contracts.runner.event.SubmissionStateEventData;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxSubmissionStateEventPublisher implements SubmissionStateEventPublisher {

  private final Outbox outbox;
  private final SubmissionProperties submissionProperties;

  private static SubmissionStateEventData toData(SubmissionStateChange change) {
    return new SubmissionStateEventData(
        change.id().value(),
        change.state(),
        change.failedTestCaseId() == null ? null : change.failedTestCaseId().value(),
        change.timeTakenMs(),
        change.memoryUsedKb(),
        change.compileOutput(),
        change.actualOutput() == null ? null : change.actualOutput().value());
  }

  @Override
  public void publish(SubmissionStateChange change) {
    this.outbox.schedule(
        OutboxSubmissionStateEventPublisher.toData(change),
        change.id().value().toString(),
        Map.of(
            OutboxKafkaRouting.TOPIC_CONTEXT_KEY,
            this.submissionProperties.stateTopic().name()));
  }
}
