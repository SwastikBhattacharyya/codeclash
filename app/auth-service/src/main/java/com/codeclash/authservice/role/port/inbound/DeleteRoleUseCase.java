package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Id;

public interface DeleteRoleUseCase {

  void delete(Id id);
}
