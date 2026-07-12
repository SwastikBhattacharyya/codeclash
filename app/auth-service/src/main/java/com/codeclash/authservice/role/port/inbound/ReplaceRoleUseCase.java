package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.ReplaceRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;

public interface ReplaceRoleUseCase {

  ReplaceRoleResult<Unresolved> replace(ReplaceRoleRequest<Unresolved> request);
}
