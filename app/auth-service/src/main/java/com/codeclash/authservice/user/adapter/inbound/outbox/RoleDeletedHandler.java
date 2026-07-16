package com.codeclash.authservice.user.adapter.inbound.outbox;

import com.codeclash.authservice.role.api.event.RoleDeletedEvent;
import com.codeclash.authservice.user.port.inbound.ReassignUsersWithoutRoleUseCase;
import io.namastack.outbox.annotation.OutboxHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDeletedHandler {

  private final ReassignUsersWithoutRoleUseCase reassignUsersWithoutRoleUseCase;

  @OutboxHandler
  public void handle(RoleDeletedEvent event) {
    this.reassignUsersWithoutRoleUseCase.reassign();
  }
}
