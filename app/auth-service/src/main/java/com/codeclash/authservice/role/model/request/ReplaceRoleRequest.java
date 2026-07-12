package com.codeclash.authservice.role.model.request;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.PermissionsState;

public record ReplaceRoleRequest<S extends PermissionsState>(
    Id id, Name name, Description description, Permissions<S> permissions) {

  public <T extends PermissionsState> ReplaceRoleRequest<T> withPermissions(
      Permissions<T> permissions) {
    return new ReplaceRoleRequest<>(this.id, this.name, this.description, permissions);
  }
}
