package com.codeclash.problemservice.contest.adapter.inbound.kafka;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.contest.event.ContestCdcEventData;
import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.model.Id;
import com.codeclash.problemservice.contest.port.inbound.SyncContestUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestCdcListener {

  private final SyncContestUseCase syncContestUseCase;

  private static Contest toContest(ContestCdcEventData data) {
    return new Contest(
        Id.of(data.id()),
        data.liveAt(),
        data.endAt(),
        data.openedAt(),
        data.questionIds() == null ? List.of() : data.questionIds(),
        new CreatedAt(data.createdAt()),
        new UpdatedAt(data.updatedAt()));
  }

  @KafkaListener(
      topics = "${codeclash.contest.cdc-consumer.topic}",
      containerFactory = "contestCdcListenerContainerFactory")
  public void handle(CdcEvent<ContestCdcEventData> event) {
    ContestCdcEventData data = event.data();
    switch (event.operation()) {
      case WRITE -> this.syncContestUseCase.write(ContestCdcListener.toContest(data));
      case DELETE -> this.syncContestUseCase.delete(Id.of(data.id()));
    }
  }
}
