package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;
import com.codeclash.authservice.role.port.inbound.PatchRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.PatchRoleUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchRoleSecuredService implements PatchRoleSecuredUseCase {

  private final PatchRoleUseCase patchRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_WRITE)
  public Role<Unresolved> patch(PatchRoleRequest<Unresolved> request) {
    return this.patchRoleUseCase.patch(request);
  }
}
