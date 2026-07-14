package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.port.inbound.DeleteUserUseCase;
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
public class DeleteUserService implements DeleteUserUseCase {

  private final UserPersistence userPersistence;
  private final UserEventPublisher userEventPublisher;

  @Override
  @Transactional
  public void delete(Id id) {
    User user =
        this.userPersistence
            .deleteById(id)
            .orElseThrow(() -> new UserNotFoundException(id.value()));
    this.userEventPublisher.deleted(user);
    DeleteUserService.log
        .atInfo()
        .setMessage("user deleted")
        .addKeyValue("user.id", user.id().value())
        .log();
  }
}
