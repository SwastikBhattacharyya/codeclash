package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.ReplaceRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;
import com.codeclash.authservice.role.port.inbound.ReplaceRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.ReplaceRoleUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceRoleSecuredService implements ReplaceRoleSecuredUseCase {

  private final ReplaceRoleUseCase replaceRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_WRITE)
  public ReplaceRoleResult<Unresolved> replace(ReplaceRoleRequest<Unresolved> request) {
    return this.replaceRoleUseCase.replace(request);
  }
}
