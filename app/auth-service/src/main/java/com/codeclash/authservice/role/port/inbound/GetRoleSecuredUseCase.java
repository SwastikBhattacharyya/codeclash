package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;

public interface GetRoleSecuredUseCase {

  Role<Unresolved> findById(Id id);
}
