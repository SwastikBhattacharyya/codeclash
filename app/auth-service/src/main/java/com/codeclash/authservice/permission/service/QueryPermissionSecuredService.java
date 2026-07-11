package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.authservice.permission.port.inbound.QueryPermissionSecuredUseCase;
import com.codeclash.authservice.permission.port.inbound.QueryPermissionUseCase;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QueryPermissionSecuredService implements QueryPermissionSecuredUseCase {

  private final QueryPermissionUseCase queryPermissionUseCase;

  @Override
  @Authorized(AuthPermissions.PERMISSION_READ)
  public PagedEntity<Permission> query(QueryPermissionRequest request) {
    return this.queryPermissionUseCase.query(request);
  }
}
