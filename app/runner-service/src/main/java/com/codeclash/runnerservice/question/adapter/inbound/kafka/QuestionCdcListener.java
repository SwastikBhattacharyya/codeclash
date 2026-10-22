package com.codeclash.runnerservice.question.adapter.inbound.kafka;

import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.MaxRamLimit;
import com.codeclash.runnerservice.question.model.MaxRunTime;
import com.codeclash.runnerservice.question.model.Question;
import com.codeclash.runnerservice.question.port.inbound.SyncQuestionUseCase;
import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.problem.event.QuestionCdcEventData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionCdcListener {

  private final SyncQuestionUseCase syncQuestionUseCase;

  private static Question toQuestion(QuestionCdcEventData data) {
    return new Question(
        Id.of(data.id()),
        MaxRunTime.of(data.maxRunTimeMs()),
        MaxRamLimit.of(data.maxRamLimitMb()),
        new UpdatedAt(data.updatedAt()));
  }

  @KafkaListener(
      topics = "${codeclash.question.cdc-consumer.topic}",
      containerFactory = "questionCdcListenerContainerFactory")
  public void handle(CdcEvent<QuestionCdcEventData> event) {
    switch (event.operation()) {
      case WRITE -> this.syncQuestionUseCase.write(QuestionCdcListener.toQuestion(event.data()));
      case DELETE -> this.syncQuestionUseCase.delete(Id.of(event.data().id()));
    }
  }
}
