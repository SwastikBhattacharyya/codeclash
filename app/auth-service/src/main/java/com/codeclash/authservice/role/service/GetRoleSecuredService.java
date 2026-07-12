package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.port.inbound.GetRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.GetRoleUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetRoleSecuredService implements GetRoleSecuredUseCase {

  private final GetRoleUseCase getRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_READ)
  public Role<Unresolved> findById(Id id) {
    return this.getRoleUseCase.findById(id);
  }
}
