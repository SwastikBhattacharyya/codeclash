package com.codeclash.contestservice.question.adapter.inbound.kafka;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contestservice.question.model.Id;
import com.codeclash.contestservice.question.model.Question;
import com.codeclash.contestservice.question.port.inbound.SyncQuestionUseCase;
import com.codeclash.contracts.problem.event.QuestionCdcEventData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionCdcListener {

  private final SyncQuestionUseCase syncQuestionUseCase;

  @KafkaListener(
      topics = "${codeclash.question.cdc-consumer.topic}",
      containerFactory = "questionCdcListenerContainerFactory")
  public void handle(CdcEvent<QuestionCdcEventData> event) {
    QuestionCdcEventData data = event.data();
    switch (event.operation()) {
      case WRITE ->
          this.syncQuestionUseCase.write(
              new Question(Id.of(data.id()), new UpdatedAt(data.updatedAt())));
      case DELETE -> this.syncQuestionUseCase.delete(Id.of(data.id()));
    }
  }
}
