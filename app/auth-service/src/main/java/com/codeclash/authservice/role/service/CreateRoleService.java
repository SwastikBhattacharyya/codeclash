package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;
import com.codeclash.authservice.role.port.inbound.CreateRoleUseCase;
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
public class CreateRoleService implements CreateRoleUseCase {

  private final RolePersistence rolePersistence;

  @Override
  @Transactional
  public Role<Unresolved> create(CreateRoleRequest<Unresolved> request) {
    Role<Unresolved> role = this.rolePersistence.create(request);
    CreateRoleService.log
        .atInfo()
        .setMessage("role created")
        .addKeyValue("role.id", role.id().value())
        .log();
    return role;
  }
}
