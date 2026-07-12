package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;
import com.codeclash.authservice.role.port.inbound.PatchRoleUseCase;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class PatchRoleService implements PatchRoleUseCase {

  private final RolePersistence rolePersistence;
  private final RoleWriteSupport roleWriteSupport;

  @Override
  @Transactional
  public Role<Unresolved> patch(PatchRoleRequest<Unresolved> request) {
    Role<Unresolved> role =
        this.rolePersistence
            .patch(request)
            .orElseThrow(() -> this.roleWriteSupport.defaultRoleModification(request.id()));
    PatchRoleService.log
        .atInfo()
        .setMessage("role patched")
        .addKeyValue("role.id", role.id().value())
        .log();
    return role;
  }
}
