package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.authservice.role.port.inbound.QueryRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.QueryRoleUseCase;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authorized;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QueryRoleSecuredService implements QueryRoleSecuredUseCase {

  private final QueryRoleUseCase queryRoleUseCase;

  @Override
  @Authorized(AuthPermissions.ROLE_READ)
  public PagedEntity<Role<Unresolved>> query(QueryRoleRequest request) {
    return this.queryRoleUseCase.query(request);
  }
}
