package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.CreateUserRequest;
import com.codeclash.authservice.user.port.inbound.CreateUserUseCase;
import com.codeclash.authservice.user.port.outbound.PasswordHasher;
import com.codeclash.authservice.user.port.outbound.UserEventPublisher;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

  private final UserPersistence userPersistence;
  private final PasswordHasher passwordHasher;
  private final UserEventPublisher userEventPublisher;
  private final UserWriteSupport userWriteSupport;

  @Override
  @Transactional
  public User create(CreateUserRequest<Raw> request) {
    User user =
        this.userPersistence.create(
            request
                .withVerified(UserWriteSupport.verifiedOrDefault(request.verified()))
                .withRole(this.userWriteSupport.roleOrDefault(request.role()))
                .withPassword(this.passwordHasher.hash(request.password())));
    this.userEventPublisher.written(user);
    CreateUserService.log
        .atInfo()
        .setMessage("user created")
        .addKeyValue("user.id", user.id().value())
        .log();
    return user;
  }
}
