package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;

public interface CreateRoleUseCase {

  Role<Unresolved> create(CreateRoleRequest<Unresolved> request);
}
