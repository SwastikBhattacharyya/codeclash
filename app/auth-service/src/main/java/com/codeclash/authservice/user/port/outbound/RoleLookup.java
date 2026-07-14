package com.codeclash.authservice.user.port.outbound;

import com.codeclash.authservice.role.api.RoleView;
import com.codeclash.authservice.user.model.RoleId;
import java.util.List;

public interface RoleLookup {

  RoleView findByName(String name);

  List<String> findPermissions(RoleId role);
}
