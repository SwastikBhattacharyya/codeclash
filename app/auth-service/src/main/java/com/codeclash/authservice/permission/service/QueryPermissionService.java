package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.authservice.permission.port.inbound.QueryPermissionUseCase;
import com.codeclash.authservice.permission.port.outbound.PermissionPersistence;
import com.codeclash.common.page.PagedEntity;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryPermissionService implements QueryPermissionUseCase {

  private final PermissionPersistence permissionPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Permission> query(QueryPermissionRequest request) {
    return this.permissionPersistence.query(request);
  }
}
