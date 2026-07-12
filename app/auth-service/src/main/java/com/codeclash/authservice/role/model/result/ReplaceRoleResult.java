package com.codeclash.authservice.role.model.result;

import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.PermissionsState;

public record ReplaceRoleResult<S extends PermissionsState>(Role<S> role, boolean created) {

  public <T extends PermissionsState> ReplaceRoleResult<T> withRole(Role<T> role) {
    return new ReplaceRoleResult<>(role, this.created);
  }
}
