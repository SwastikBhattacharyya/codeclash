package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.port.inbound.GetPermissionSecuredUseCase;
import com.codeclash.authservice.permission.port.inbound.GetPermissionUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetPermissionSecuredService implements GetPermissionSecuredUseCase {

  private final GetPermissionUseCase getPermissionUseCase;

  @Override
  @Authorized(AuthPermissions.PERMISSION_READ)
  public Permission findById(Id id) {
    return this.getPermissionUseCase.findById(id);
  }
}
