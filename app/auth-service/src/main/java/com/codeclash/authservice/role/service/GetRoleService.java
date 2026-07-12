package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.exception.RoleNotFoundException;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.port.inbound.GetRoleUseCase;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetRoleService implements GetRoleUseCase {

  private final RolePersistence rolePersistence;

  @Override
  public Role<Unresolved> findById(Id id) {
    return this.rolePersistence
        .findById(id)
        .orElseThrow(() -> new RoleNotFoundException(id.value()));
  }
}
