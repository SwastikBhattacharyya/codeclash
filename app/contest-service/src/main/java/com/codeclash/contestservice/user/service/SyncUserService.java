package com.codeclash.contestservice.user.service;

import com.codeclash.contestservice.user.model.Id;
import com.codeclash.contestservice.user.model.User;
import com.codeclash.contestservice.user.port.inbound.SyncUserUseCase;
import com.codeclash.contestservice.user.port.outbound.ContestLeaderboards;
import com.codeclash.contestservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncUserService implements SyncUserUseCase {

  private final UserPersistence userPersistence;
  private final ContestLeaderboards contestLeaderboards;

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
  @Transactional
  public void delete(Id id) {
    List<UUID> contestIds = this.contestLeaderboards.findRegisteredContestIds(id);
    this.contestLeaderboards.lock(contestIds);
    this.userPersistence.deleteById(id);
    this.contestLeaderboards.rebuild(contestIds);
    SyncUserService.log
        .atDebug()
        .setMessage("user removed")
        .addKeyValue("user.id", id.value())
        .addKeyValue("contest.count", contestIds.size())
        .log();
  }
}
