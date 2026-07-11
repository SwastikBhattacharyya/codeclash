package com.codeclash.authservice.permission.port.inbound;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;

public interface GetPermissionUseCase {

  Permission findById(Id id);
}
