package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.port.inbound.DeleteRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.DeleteRoleUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteRoleSecuredService implements DeleteRoleSecuredUseCase {

  private final DeleteRoleUseCase deleteRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_WRITE)
  public void delete(Id id) {
    this.deleteRoleUseCase.delete(id);
  }
}
