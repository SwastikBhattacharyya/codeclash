package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;

public interface PatchRoleUseCase {

  Role<Unresolved> patch(PatchRoleRequest<Unresolved> request);
}
