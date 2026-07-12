package com.codeclash.authservice.role.port.outbound;

import com.codeclash.authservice.permission.api.PermissionView;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.common.permission.Name;
import java.util.List;

public interface PermissionLookup {

  List<PermissionView> findByIds(List<PermissionId> ids);

  List<PermissionView> findByNames(List<Name> names);
}
