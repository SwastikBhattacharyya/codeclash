package com.codeclash.problemservice.question.adapter.outbound.outbox;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.Operation;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contracts.problem.event.QuestionCdcEventData;
import com.codeclash.problemservice.question.adapter.config.QuestionProperties;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.port.outbound.QuestionEventPublisher;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxQuestionEventPublisher implements QuestionEventPublisher {

  private final Outbox outbox;
  private final QuestionProperties questionProperties;

  @Override
  public void written(Question question) {
    this.publish(Operation.WRITE, question);
  }

  @Override
  public void deleted(Question question) {
    this.publish(Operation.DELETE, question);
  }

  private void publish(
      Operation operation, Question question) {
    this.outbox.schedule(
        new CdcEvent<>(operation, OutboxQuestionEventPublisher.toData(question)),
        question.id().value().toString(),
        Map.of(OutboxKafkaRouting.TOPIC_CONTEXT_KEY, this.questionProperties.cdcTopic().name()));
  }

  private static QuestionCdcEventData toData(
      Question question) {
    return new QuestionCdcEventData(
        question.id().value(),
        question.visible().value(),
        question.maxRunTime().value(),
        question.maxRamLimit().value(),
        question.createdAt().value(),
        question.updatedAt().value());
  }
}
