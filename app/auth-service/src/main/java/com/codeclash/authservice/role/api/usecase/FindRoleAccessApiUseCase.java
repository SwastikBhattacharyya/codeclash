package com.codeclash.authservice.role.api.usecase;

import com.codeclash.authservice.role.api.RoleAccessView;
import java.util.List;
import java.util.UUID;

public interface FindRoleAccessApiUseCase {

  List<RoleAccessView> findByIds(List<UUID> ids);
}
