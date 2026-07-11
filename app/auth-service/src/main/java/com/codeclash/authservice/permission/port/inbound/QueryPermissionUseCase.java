package com.codeclash.authservice.permission.port.inbound;

import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.common.page.PagedEntity;

public interface QueryPermissionUseCase {

  PagedEntity<Permission> query(QueryPermissionRequest request);
}
