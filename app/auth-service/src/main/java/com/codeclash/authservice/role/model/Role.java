package com.codeclash.authservice.role.model;

import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.PermissionsState;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;

public record Role<S extends PermissionsState>(
    Id id,
    Name name,
    Description description,
    Default isDefault,
    Permissions<S> permissions,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {

  public <T extends PermissionsState> Role<T> withPermissions(Permissions<T> permissions) {
    return new Role<>(
        this.id,
        this.name,
        this.description,
        this.isDefault,
        permissions,
        this.createdAt,
        this.updatedAt);
  }
}
