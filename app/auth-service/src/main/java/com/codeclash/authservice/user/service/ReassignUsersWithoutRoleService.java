package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.port.inbound.GetDefaultRoleUseCase;
import com.codeclash.authservice.user.port.inbound.ReassignUsersWithoutRoleUseCase;
import com.codeclash.authservice.user.port.outbound.UserEventPublisher;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ReassignUsersWithoutRoleService implements ReassignUsersWithoutRoleUseCase {

  private final UserPersistence userPersistence;
  private final GetDefaultRoleUseCase getDefaultRoleUseCase;
  private final UserEventPublisher userEventPublisher;

  @Override
  @Transactional
  public void reassign() {
    List<User> users =
        this.userPersistence.assignRoleWhereMissing(this.getDefaultRoleUseCase.get());
    users.forEach(this.userEventPublisher::written);
    ReassignUsersWithoutRoleService.log
        .atInfo()
        .setMessage("users reassigned to default role")
        .addKeyValue("count", users.size())
        .log();
  }
}
