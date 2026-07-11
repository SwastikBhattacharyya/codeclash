package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.exception.PermissionNotFoundException;
import com.codeclash.authservice.permission.port.inbound.GetPermissionUseCase;
import com.codeclash.authservice.permission.port.outbound.PermissionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetPermissionService implements GetPermissionUseCase {

  private final PermissionPersistence permissionPersistence;

  @Override
  public Permission findById(Id id) {
    return this.permissionPersistence
        .findById(id)
        .orElseThrow(() -> new PermissionNotFoundException(id.value()));
  }
}
