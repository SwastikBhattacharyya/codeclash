package com.codeclash.contestservice.user.adapter.inbound.kafka;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.auth.event.UserCdcEventData;
import com.codeclash.contestservice.user.model.Id;
import com.codeclash.contestservice.user.model.User;
import com.codeclash.contestservice.user.model.Username;
import com.codeclash.contestservice.user.port.inbound.SyncUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCdcListener {

  private final SyncUserUseCase syncUserUseCase;

  @KafkaListener(
      topics = "${codeclash.user.cdc-consumer.topic}",
      containerFactory = "userCdcListenerContainerFactory")
  public void handle(CdcEvent<UserCdcEventData> event) {
    UserCdcEventData data = event.data();
    switch (event.operation()) {
      case WRITE ->
          this.syncUserUseCase.write(
              new User(
                  Id.of(data.id()),
                  Username.of(data.username()),
                  new CreatedAt(data.createdAt()),
                  new UpdatedAt(data.updatedAt())));
      case DELETE -> this.syncUserUseCase.delete(Id.of(data.id()));
    }
  }
}
