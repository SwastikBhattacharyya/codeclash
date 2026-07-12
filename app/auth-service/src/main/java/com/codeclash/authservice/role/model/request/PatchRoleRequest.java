package com.codeclash.authservice.role.model.request;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.PermissionsState;
import com.codeclash.common.patch.PatchObject;

public record PatchRoleRequest<S extends PermissionsState>(
    Id id,
    PatchObject<Name> name,
    PatchObject<Description> description,
    PatchObject<Permissions<S>> permissions) {

  public <T extends PermissionsState> PatchRoleRequest<T> withPermissions(
      PatchObject<Permissions<T>> permissions) {
    return new PatchRoleRequest<>(this.id, this.name, this.description, permissions);
  }
}
