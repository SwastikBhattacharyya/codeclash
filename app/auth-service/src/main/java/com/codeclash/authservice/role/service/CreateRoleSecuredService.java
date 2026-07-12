package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;
import com.codeclash.authservice.role.port.inbound.CreateRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.CreateRoleUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateRoleSecuredService implements CreateRoleSecuredUseCase {

  private final CreateRoleUseCase createRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_WRITE)
  public Role<Unresolved> create(CreateRoleRequest<Unresolved> request) {
    return this.createRoleUseCase.create(request);
  }
}
