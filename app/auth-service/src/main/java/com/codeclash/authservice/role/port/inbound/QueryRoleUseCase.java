package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.common.page.PagedEntity;

public interface QueryRoleUseCase {

  PagedEntity<Role<Unresolved>> query(QueryRoleRequest request);
}
