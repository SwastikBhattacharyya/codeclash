package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.RoleId;

public interface GetDefaultRoleUseCase {

  RoleId get();
}
