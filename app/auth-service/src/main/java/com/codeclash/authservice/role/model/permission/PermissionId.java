package com.codeclash.authservice.role.model.permission;

import com.codeclash.authservice.role.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.role.model.exception.NullPermissionsException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class PermissionId {

  public static final int VERSION = 7;

  private final UUID value;

  private PermissionId(UUID value) {
    this.value = value;
  }

  public static PermissionId of(UUID value) {
    if (value == null) throw new NullPermissionsException();
    if (value.version() != PermissionId.VERSION) throw new InvalidPermissionIdException(value);
    return new PermissionId(value);
  }

  public static PermissionId unsafe(UUID value) {
    return new PermissionId(value);
  }

  public UUID value() {
    return this.value;
  }
}
