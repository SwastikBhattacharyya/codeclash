package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.exception.DefaultRoleModificationException;
import com.codeclash.authservice.role.port.inbound.GetRoleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RoleWriteSupport {

  private final GetRoleUseCase getRoleUseCase;

  DefaultRoleModificationException defaultRoleModification(Id id) {
    this.getRoleUseCase.findById(id);
    return new DefaultRoleModificationException(id.value());
  }
}
