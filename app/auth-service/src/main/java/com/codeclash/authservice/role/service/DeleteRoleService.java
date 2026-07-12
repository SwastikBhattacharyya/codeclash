package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.port.inbound.DeleteRoleUseCase;
import com.codeclash.authservice.role.port.outbound.RoleEventPublisher;
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
public class DeleteRoleService implements DeleteRoleUseCase {

  private final RolePersistence rolePersistence;
  private final RoleEventPublisher roleEventPublisher;
  private final RoleWriteSupport roleWriteSupport;

  @Override
  @Transactional
  public void delete(Id id) {
    if (!this.rolePersistence.deleteById(id))
      throw this.roleWriteSupport.defaultRoleModification(id);
    this.roleEventPublisher.roleDeleted(id);
    DeleteRoleService.log
        .atInfo()
        .setMessage("role deleted")
        .addKeyValue("role.id", id.value())
        .log();
  }
}
