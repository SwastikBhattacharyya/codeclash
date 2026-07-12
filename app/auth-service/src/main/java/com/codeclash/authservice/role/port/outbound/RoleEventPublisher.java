package com.codeclash.authservice.role.port.outbound;

import com.codeclash.authservice.role.model.Id;

public interface RoleEventPublisher {

  void roleDeleted(Id id);
}
