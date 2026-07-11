package com.codeclash.authservice.permission.api.usecase;

import com.codeclash.authservice.permission.api.PermissionView;
import java.util.List;
import java.util.UUID;

public interface FindPermissionsByIdsApiUseCase {

  List<PermissionView> findByIds(List<UUID> ids);
}
