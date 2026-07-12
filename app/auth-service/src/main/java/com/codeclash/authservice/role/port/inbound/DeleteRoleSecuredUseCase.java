package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Id;

public interface DeleteRoleSecuredUseCase {

  void delete(Id id);
}
