package com.codeclash.authservice.user.api.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FindUserRolesApiUseCase {

  Map<UUID, UUID> findRoleIds(List<UUID> userIds);
}
