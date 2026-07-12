package com.codeclash.authservice.role.port.inbound;

import com.codeclash.authservice.role.model.permission.Named;
import com.codeclash.authservice.role.model.request.SyncDefaultRoleRequest;
import java.util.List;

public interface SyncDefaultRolesUseCase {

  void sync(List<SyncDefaultRoleRequest<Named>> requests);
}
