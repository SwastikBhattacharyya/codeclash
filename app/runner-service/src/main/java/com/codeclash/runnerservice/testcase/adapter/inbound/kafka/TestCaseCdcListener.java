package com.codeclash.runnerservice.testcase.adapter.inbound.kafka;

import com.codeclash.runnerservice.testcase.model.Id;
import com.codeclash.runnerservice.testcase.model.Input;
import com.codeclash.runnerservice.testcase.model.Output;
import com.codeclash.runnerservice.testcase.model.Public;
import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import com.codeclash.runnerservice.testcase.port.inbound.SyncTestCaseUseCase;
import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.problem.event.TestCaseCdcEventData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestCaseCdcListener {

  private final SyncTestCaseUseCase syncTestCaseUseCase;

  private static TestCase toTestCase(TestCaseCdcEventData data) {
    return new TestCase(
        Id.of(data.id()),
        new Input(data.input()),
        new Output(data.output()),
        new Public(data.isPublic()),
        QuestionId.of(data.questionId()),
        new CreatedAt(data.createdAt()),
        new UpdatedAt(data.updatedAt()));
  }

  @KafkaListener(
      topics = "${codeclash.test-case.cdc-consumer.topic}",
      containerFactory = "testCaseCdcListenerContainerFactory")
  public void handle(CdcEvent<TestCaseCdcEventData> event) {
    switch (event.operation()) {
      case WRITE -> this.syncTestCaseUseCase.write(TestCaseCdcListener.toTestCase(event.data()));
      case DELETE -> this.syncTestCaseUseCase.delete(Id.of(event.data().id()));
    }
  }
}
