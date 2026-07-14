package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;
import com.codeclash.authservice.user.port.inbound.ReplaceUserUseCase;
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
public class ReplaceUserService implements ReplaceUserUseCase {

  private final UserPersistence userPersistence;
  private final PasswordHasher passwordHasher;
  private final UserEventPublisher userEventPublisher;
  private final UserWriteSupport userWriteSupport;

  @Override
  @Transactional
  public ReplaceUserResult replace(ReplaceUserRequest<Raw> request) {
    Verified verified = UserWriteSupport.verifiedOrDefault(request.verified());
    if (request.verificationToken() != null && verified.value())
      UserWriteSupport.requireTokenMatch(
          this.userPersistence.findById(request.id()), verified, request.verificationToken());
    ReplaceUserResult result =
        this.userPersistence.replace(
            request
                .withVerified(verified)
                .withRole(this.userWriteSupport.roleOrDefault(request.role()))
                .withPassword(this.passwordHasher.hash(request.password())));
    this.userEventPublisher.written(result.user());
    ReplaceUserService.log
        .atInfo()
        .setMessage("user replaced")
        .addKeyValue("user.id", result.user().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}
