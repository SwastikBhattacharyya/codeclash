package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.PatchUserRequest;
import com.codeclash.authservice.user.port.inbound.PatchUserUseCase;
import com.codeclash.authservice.user.port.outbound.PasswordHasher;
import com.codeclash.authservice.user.port.outbound.UserEventPublisher;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class PatchUserService implements PatchUserUseCase {

  private final UserPersistence userPersistence;
  private final PasswordHasher passwordHasher;
  private final UserEventPublisher userEventPublisher;

  @Override
  @Transactional
  public User patch(PatchUserRequest<Raw> request) {
    Verified verified = request.verified().orElse(null);
    if (request.verificationToken() != null && verified != null && verified.value())
      UserWriteSupport.requireTokenMatch(
          Optional.of(
              this.userPersistence
                  .findById(request.id())
                  .orElseThrow(() -> new UserNotFoundException(request.id().value()))),
          verified,
          request.verificationToken());
    User user =
        this.userPersistence
            .patch(request.withPassword(request.password().map(this.passwordHasher::hash)))
            .orElseThrow(() -> new UserNotFoundException(request.id().value()));
    this.userEventPublisher.written(user);
    PatchUserService.log
        .atInfo()
        .setMessage("user patched")
        .addKeyValue("user.id", user.id().value())
        .log();
    return user;
  }
}
