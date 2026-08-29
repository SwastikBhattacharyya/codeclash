package com.codeclash.problemservice.submission.adapter.outbound.outbox;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.Operation;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contracts.problem.event.SubmissionCdcEventData;
import com.codeclash.problemservice.submission.adapter.config.SubmissionProperties;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.port.outbound.SubmissionEventPublisher;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxSubmissionEventPublisher implements SubmissionEventPublisher {

  private final Outbox outbox;
  private final SubmissionProperties submissionProperties;

  @Override
  public void written(Submission submission) {
    this.publish(Operation.WRITE, submission);
  }

  @Override
  public void deleted(Submission submission) {
    this.publish(Operation.DELETE, submission);
  }

  private void publish(Operation operation, Submission submission) {
    this.outbox.schedule(
        new CdcEvent<>(operation, OutboxSubmissionEventPublisher.toData(submission)),
        submission.id().value().toString(),
        Map.of(OutboxKafkaRouting.TOPIC_CONTEXT_KEY, this.submissionProperties.cdcTopic().name()));
  }

  private static SubmissionCdcEventData toData(Submission submission) {
    return new SubmissionCdcEventData(
        submission.id().value(),
        submission.language(),
        submission.code().value(),
        submission.state(),
        submission.failedTestCaseId() == null ? null : submission.failedTestCaseId().value(),
        submission.timeTaken() == null ? null : submission.timeTaken().value(),
        submission.memoryUsed() == null ? null : submission.memoryUsed().value(),
        submission.compileOutput() == null ? null : submission.compileOutput().value(),
        submission.actualOutput() == null ? null : submission.actualOutput().value(),
        submission.questionId().value(),
        submission.userId().value(),
        submission.createdAt().value(),
        submission.updatedAt().value());
  }
}
