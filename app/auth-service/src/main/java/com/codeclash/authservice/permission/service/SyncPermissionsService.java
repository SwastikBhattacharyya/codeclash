package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.model.exception.DuplicatePermissionNameException;
import com.codeclash.authservice.permission.port.inbound.SyncPermissionsUseCase;
import com.codeclash.authservice.permission.port.outbound.PermissionPersistence;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import com.codeclash.common.permission.PermissionList;
import io.micrometer.observation.annotation.Observed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncPermissionsService implements SyncPermissionsUseCase {

  private final PermissionPersistence permissionPersistence;
  private final List<PermissionList> permissionLists;

  @Override
  @Transactional
  public void sync() {
    List<PermissionDefinition> definitions =
        this.permissionLists.stream().flatMap(list -> list.permissions().stream()).toList();
    List<Name> names = SyncPermissionsService.uniqueNames(definitions);

    this.permissionPersistence.lock();
    this.permissionPersistence.deleteByNameNotIn(names);
    definitions.forEach(this.permissionPersistence::upsert);
    SyncPermissionsService.log
        .atInfo()
        .setMessage("permissions synced")
        .addKeyValue("count", definitions.size())
        .log();
  }

  private static List<Name> uniqueNames(List<PermissionDefinition> definitions) {
    Set<Name> names = new HashSet<>();
    for (PermissionDefinition definition : definitions) {
      if (!names.add(definition.name()))
        throw new DuplicatePermissionNameException(definition.name().value());
    }
    return List.copyOf(names);
  }
}
