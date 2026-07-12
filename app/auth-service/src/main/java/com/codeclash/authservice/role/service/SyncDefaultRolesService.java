package com.codeclash.authservice.role.service;

import com.codeclash.authservice.permission.api.PermissionView;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.exception.DuplicateRoleNameException;
import com.codeclash.authservice.role.model.permission.Named;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.SyncDefaultRoleRequest;
import com.codeclash.authservice.role.port.inbound.SyncDefaultRolesUseCase;
import com.codeclash.authservice.role.port.outbound.PermissionLookup;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
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
public class SyncDefaultRolesService implements SyncDefaultRolesUseCase {

  private final RolePersistence rolePersistence;
  private final PermissionLookup permissionLookup;

  @Override
  @Transactional
  public void sync(List<SyncDefaultRoleRequest<Named>> requests) {
    List<Name> names = SyncDefaultRolesService.uniqueNames(requests);

    this.rolePersistence.lock();
    this.rolePersistence.demoteDefaultsByNameNotIn(names);
    for (SyncDefaultRoleRequest<Named> request : requests)
      this.rolePersistence.upsertDefault(
          request.withPermissions(this.resolveNamed(request.permissions())));
    SyncDefaultRolesService.log
        .atInfo()
        .setMessage("default roles synced")
        .addKeyValue("count", requests.size())
        .log();
  }

  private Permissions<Unresolved> resolveNamed(Permissions<Named> permissions) {
    return Permissions.unresolved(
        this.permissionLookup.findByNames(permissions.value().names()).stream()
            .map(PermissionView::id)
            .toList());
  }

  private static List<Name> uniqueNames(List<SyncDefaultRoleRequest<Named>> requests) {
    Set<Name> names = new HashSet<>();
    for (SyncDefaultRoleRequest<Named> request : requests) {
      if (!names.add(request.name()))
        throw new DuplicateRoleNameException(request.name().value());
    }
    return List.copyOf(names);
  }
}
