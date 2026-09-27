package com.codeclash.contestservice.contest.adapter.outbound.outbox;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.Operation;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contestservice.contest.adapter.config.ContestProperties;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.port.outbound.ContestEventPublisher;
import com.codeclash.contracts.contest.event.ContestCdcEventData;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxContestEventPublisher implements ContestEventPublisher {

  private final Outbox outbox;
  private final ContestProperties contestProperties;

  private static ContestCdcEventData toData(Contest contest) {
    return new ContestCdcEventData(
        contest.id().value(),
        contest.liveAt().value(),
        contest.endAt().value(),
        contest.openedAt() == null ? null : contest.openedAt().value(),
        contest.questions().questionIds(),
        contest.createdAt().value(),
        contest.updatedAt().value());
  }

  @Override
  public void written(Contest contest) {
    this.publish(Operation.WRITE, contest);
  }

  @Override
  public void deleted(Contest contest) {
    this.publish(Operation.DELETE, contest);
  }

  private void publish(Operation operation, Contest contest) {
    this.outbox.schedule(
        new CdcEvent<>(operation, OutboxContestEventPublisher.toData(contest)),
        contest.id().value().toString(),
        Map.of(OutboxKafkaRouting.TOPIC_CONTEXT_KEY, this.contestProperties.cdcTopic().name()));
  }
}
