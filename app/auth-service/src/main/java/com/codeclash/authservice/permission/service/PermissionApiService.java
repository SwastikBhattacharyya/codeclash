package com.codeclash.authservice.permission.service;

import com.codeclash.authservice.permission.api.PermissionView;
import com.codeclash.authservice.permission.api.exception.PermissionNamesNotFoundException;
import com.codeclash.authservice.permission.api.exception.PermissionsNotFoundException;
import com.codeclash.authservice.permission.api.usecase.FindPermissionsByIdsApiUseCase;
import com.codeclash.authservice.permission.api.usecase.FindPermissionsByNamesApiUseCase;
import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.port.outbound.PermissionPersistence;
import com.codeclash.common.permission.Name;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PermissionApiService
    implements FindPermissionsByIdsApiUseCase, FindPermissionsByNamesApiUseCase {

  private final PermissionPersistence permissionPersistence;

  @Override
  public List<PermissionView> findByIds(List<UUID> ids) {
    if (ids.isEmpty()) return List.of();

    List<UUID> distinct = ids.stream().distinct().toList();
    List<Permission> permissions =
        this.permissionPersistence.findAllByIdIn(distinct.stream().map(Id::unsafe).toList());

    Set<UUID> found =
        permissions.stream().map(permission -> permission.id().value()).collect(Collectors.toSet());
    List<UUID> missing = distinct.stream().filter(id -> !found.contains(id)).toList();
    if (!missing.isEmpty()) throw new PermissionsNotFoundException(missing);

    return permissions.stream().map(PermissionApiService::toView).toList();
  }

  @Override
  public List<PermissionView> findByNames(List<String> names) {
    if (names.isEmpty()) return List.of();

    List<String> distinct = names.stream().distinct().toList();
    List<Permission> permissions =
        this.permissionPersistence.findAllByNameIn(distinct.stream().map(Name::unsafe).toList());

    Set<String> found =
        permissions.stream()
            .map(permission -> permission.name().value())
            .collect(Collectors.toSet());
    List<String> missing = distinct.stream().filter(name -> !found.contains(name)).toList();
    if (!missing.isEmpty()) throw new PermissionNamesNotFoundException(missing);

    return permissions.stream().map(PermissionApiService::toView).toList();
  }

  private static PermissionView toView(Permission permission) {
    return new PermissionView(
        permission.id().value(),
        permission.name().value(),
        permission.description().value(),
        permission.createdAt().value(),
        permission.updatedAt().value());
  }
}
