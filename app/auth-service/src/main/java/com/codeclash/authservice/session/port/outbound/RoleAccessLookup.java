package com.codeclash.authservice.session.port.outbound;

import com.codeclash.authservice.role.api.RoleAccessView;
import java.util.List;
import java.util.UUID;

public interface RoleAccessLookup {

  List<RoleAccessView> findByIds(List<UUID> ids);
}
