package com.codeclash.problemservice.user.service;

import com.codeclash.problemservice.user.model.Id;
import com.codeclash.problemservice.user.model.User;
import com.codeclash.problemservice.user.port.inbound.SyncUserUseCase;
import com.codeclash.problemservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncUserService implements SyncUserUseCase {

  private final UserPersistence userPersistence;

  @Override
  public void write(User user) {
    this.userPersistence.upsert(user);
    SyncUserService.log
        .atDebug()
        .setMessage("user synced")
        .addKeyValue("user.id", user.id().value())
        .log();
  }

  @Override
  public void delete(Id id) {
    this.userPersistence.deleteById(id);
    SyncUserService.log
        .atDebug()
        .setMessage("user removed")
        .addKeyValue("user.id", id.value())
        .log();
  }
}
