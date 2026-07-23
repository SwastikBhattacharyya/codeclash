package com.codeclash.notificationservice.user.adapter.inbound.kafka;

import com.codeclash.notificationservice.user.model.Email;
import com.codeclash.notificationservice.user.model.Id;
import com.codeclash.notificationservice.user.model.Name;
import com.codeclash.notificationservice.user.model.User;
import com.codeclash.notificationservice.user.model.VerificationToken;
import com.codeclash.notificationservice.user.model.Verified;
import com.codeclash.notificationservice.user.port.inbound.SendUserVerificationMailUseCase;
import com.codeclash.common.event.CdcEvent;
import com.codeclash.contracts.auth.event.UserCdcEventData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCdcListener {

  private final SendUserVerificationMailUseCase sendUserVerificationMailUseCase;

  private static User toUser(UserCdcEventData data) {
    return new User(
        Id.of(data.id()),
        new Name(data.name()),
        new Email(data.email()),
        new Verified(data.verified()),
        data.verificationToken() == null ? null : VerificationToken.of(data.verificationToken()));
  }

  @KafkaListener(
      topics = "${codeclash.user.cdc-consumer.topic}",
      containerFactory = "userCdcListenerContainerFactory")
  public void handle(CdcEvent<UserCdcEventData> event) {
    switch (event.operation()) {
      case WRITE -> this.sendUserVerificationMailUseCase.send(UserCdcListener.toUser(event.data()));
      case DELETE -> {}
    }
  }
}
