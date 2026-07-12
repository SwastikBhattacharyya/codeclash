package com.codeclash.authservice.role.service;

import com.codeclash.authservice.permission.api.PermissionView;
import com.codeclash.authservice.role.api.RoleAccessView;
import com.codeclash.authservice.role.api.RoleView;
import com.codeclash.authservice.role.api.exception.RoleNameNotFoundException;
import com.codeclash.authservice.role.api.usecase.FindRoleAccessApiUseCase;
import com.codeclash.authservice.role.api.usecase.FindRoleByNameApiUseCase;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.port.outbound.PermissionLookup;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class RoleApiService implements FindRoleByNameApiUseCase, FindRoleAccessApiUseCase {

  private final RolePersistence rolePersistence;
  private final PermissionLookup permissionLookup;

  @Override
  public RoleView findByName(String name) {
    return this.rolePersistence
        .findByName(Name.unsafe(name))
        .map(RoleApiService::toView)
        .orElseThrow(() -> new RoleNameNotFoundException(name));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleAccessView> findByIds(List<UUID> ids) {
    if (ids.isEmpty()) return List.of();
    List<Role<Unresolved>> roles =
        this.rolePersistence.findAllByIdIn(ids.stream().map(Id::unsafe).toList());
    List<PermissionId> permissionIds =
        roles.stream()
            .flatMap(role -> role.permissions().value().ids().stream())
            .distinct()
            .toList();
    Map<UUID, String> names =
        this.permissionLookup.findByIds(permissionIds).stream()
            .collect(Collectors.toMap(PermissionView::id, PermissionView::name));
    return roles.stream()
        .map(
            role ->
                new RoleAccessView(
                    role.id().value(),
                    role.name().value(),
                    role.permissions().value().ids().stream()
                        .map(PermissionId::value)
                        .map(names::get)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList()))
        .toList();
  }

  private static RoleView toView(Role<Unresolved> role) {
    return new RoleView(
        role.id().value(),
        role.name().value(),
        role.description().value(),
        role.isDefault().value(),
        role.createdAt().value(),
        role.updatedAt().value());
  }
}
