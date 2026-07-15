package com.codeclash.authservice.user.adapter.outbound.outbox;

import com.codeclash.authservice.user.adapter.config.UserProperties;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.port.outbound.UserEventPublisher;
import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.Operation;
import com.codeclash.common.event.OutboxKafkaRouting;
import com.codeclash.contracts.auth.event.UserCdcEventData;
import io.namastack.outbox.Outbox;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxUserEventPublisher implements UserEventPublisher {

  private final Outbox outbox;
  private final UserProperties userProperties;

  @Override
  public void written(User user) {
    this.publish(Operation.WRITE, user);
  }

  @Override
  public void deleted(User user) {
    this.publish(Operation.DELETE, user);
  }

  private void publish(Operation operation, User user) {
    this.outbox.schedule(
        new CdcEvent<>(operation, OutboxUserEventPublisher.toData(user)),
        user.id().value().toString(),
        Map.of(OutboxKafkaRouting.TOPIC_CONTEXT_KEY, this.userProperties.cdcTopic().name()));
  }

  private static UserCdcEventData toData(User user) {
    return new UserCdcEventData(
        user.id().value(),
        user.name().value(),
        user.username().value(),
        user.email().value(),
        user.verified().value(),
        user.verificationToken() == null ? null : user.verificationToken().value(),
        user.role() == null ? null : user.role().value(),
        user.createdAt().value(),
        user.updatedAt().value());
  }
}
