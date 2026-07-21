package com.codeclash.notificationservice.user.service;

import com.codeclash.notificationservice.user.model.User;
import com.codeclash.notificationservice.user.model.exception.NullVerificationTokenException;
import com.codeclash.notificationservice.user.port.inbound.SendUserVerificationMailUseCase;
import com.codeclash.notificationservice.user.port.outbound.UserVerificationMailPersistence;
import com.codeclash.notificationservice.user.port.outbound.UserVerificationMailSender;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SendUserVerificationMailService implements SendUserVerificationMailUseCase {

  private final UserVerificationMailPersistence userVerificationMailPersistence;
  private final UserVerificationMailSender userVerificationMailSender;

  @Override
  @Transactional
  public void send(User user) {
    if (user.verified().value()) return;
    if (user.verificationToken() == null) throw new NullVerificationTokenException();
    if (!this.userVerificationMailPersistence.claim(user)) return;
    this.userVerificationMailSender.send(user);
    SendUserVerificationMailService.log
        .atInfo()
        .setMessage("verification mail sent")
        .addKeyValue("user.id", user.id().value())
        .log();
  }
}
