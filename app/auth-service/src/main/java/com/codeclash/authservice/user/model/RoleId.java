package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.user.model.exception.NullRoleException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class RoleId {

  public static final int VERSION = 7;

  private final UUID value;

  private RoleId(UUID value) {
    this.value = value;
  }

  public static RoleId of(UUID value) {
    if (value == null) throw new NullRoleException();
    if (value.version() != RoleId.VERSION) throw new InvalidRoleIdException(value);
    return new RoleId(value);
  }

  public static RoleId unsafe(UUID value) {
    return new RoleId(value);
  }

  public UUID value() {
    return this.value;
  }
}
