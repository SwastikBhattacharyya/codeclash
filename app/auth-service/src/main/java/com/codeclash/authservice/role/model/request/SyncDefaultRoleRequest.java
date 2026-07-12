package com.codeclash.authservice.role.model.request;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.PermissionsState;

public record SyncDefaultRoleRequest<S extends PermissionsState>(
    Name name, Description description, Permissions<S> permissions) {

  public <T extends PermissionsState> SyncDefaultRoleRequest<T> withPermissions(
      Permissions<T> permissions) {
    return new SyncDefaultRoleRequest<>(this.name, this.description, permissions);
  }
}
