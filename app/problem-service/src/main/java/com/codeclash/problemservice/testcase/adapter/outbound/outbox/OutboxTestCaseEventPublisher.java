package com.codeclash.problemservice.testcase.adapter.outbound.outbox;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.Operation;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contracts.problem.event.TestCaseCdcEventData;
import com.codeclash.problemservice.testcase.adapter.config.TestCaseProperties;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.port.outbound.TestCaseEventPublisher;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxTestCaseEventPublisher implements TestCaseEventPublisher {

  private final Outbox outbox;
  private final TestCaseProperties testCaseProperties;

  @Override
  public void written(TestCase testCase) {
    this.publish(Operation.WRITE, testCase);
  }

  @Override
  public void deleted(TestCase testCase) {
    this.publish(Operation.DELETE, testCase);
  }

  private void publish(Operation operation, TestCase testCase) {
    this.outbox.schedule(
        new CdcEvent<>(operation, OutboxTestCaseEventPublisher.toData(testCase)),
        testCase.questionId().value().toString(),
        Map.of(OutboxKafkaRouting.TOPIC_CONTEXT_KEY, this.testCaseProperties.cdcTopic().name()));
  }

  private static TestCaseCdcEventData toData(TestCase testCase) {
    return new TestCaseCdcEventData(
        testCase.id().value(),
        testCase.input().value(),
        testCase.output().value(),
        testCase.isPublic().value(),
        testCase.questionId().value(),
        testCase.createdAt().value(),
        testCase.updatedAt().value());
  }
}
