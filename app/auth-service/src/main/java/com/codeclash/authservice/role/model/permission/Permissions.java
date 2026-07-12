package com.codeclash.authservice.role.model.permission;

import com.codeclash.authservice.role.model.exception.NullPermissionsException;
import com.codeclash.common.permission.Name;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Permissions<S extends PermissionsState> {

  private final S value;

  private Permissions(S value) {
    this.value = value;
  }

  public static Permissions<Unresolved> unresolved(List<UUID> ids) {
    if (ids == null || ids.stream().anyMatch(Objects::isNull))
      throw new NullPermissionsException();
    return new Permissions<>(
        new Unresolved(ids.stream().map(PermissionId::of).distinct().toList()));
  }

  public static Permissions<Named> named(List<Name> names) {
    if (names == null || names.stream().anyMatch(Objects::isNull))
      throw new NullPermissionsException();
    return new Permissions<>(new Named(names.stream().distinct().toList()));
  }

  public S value() {
    return this.value;
  }
}
