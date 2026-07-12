package com.codeclash.authservice.role.model.request;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.PermissionsState;

public record CreateRoleRequest<S extends PermissionsState>(
    Name name, Description description, Permissions<S> permissions) {

  public <T extends PermissionsState> CreateRoleRequest<T> withPermissions(
      Permissions<T> permissions) {
    return new CreateRoleRequest<>(this.name, this.description, permissions);
  }
}
