package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.authservice.role.port.inbound.QueryRoleUseCase;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import com.codeclash.common.page.PagedEntity;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryRoleService implements QueryRoleUseCase {

  private final RolePersistence rolePersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Role<Unresolved>> query(QueryRoleRequest request) {
    return this.rolePersistence.query(request);
  }
}
