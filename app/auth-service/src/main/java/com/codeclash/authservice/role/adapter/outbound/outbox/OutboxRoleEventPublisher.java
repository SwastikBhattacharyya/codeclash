package com.codeclash.authservice.role.adapter.outbound.outbox;

import com.codeclash.authservice.role.api.event.RoleDeletedEvent;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.port.outbound.RoleEventPublisher;
import io.namastack.outbox.Outbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRoleEventPublisher implements RoleEventPublisher {

  private final Outbox outbox;

  @Override
  public void roleDeleted(Id id) {
    this.outbox.schedule(new RoleDeletedEvent(id.value()), id.value().toString());
  }
}
