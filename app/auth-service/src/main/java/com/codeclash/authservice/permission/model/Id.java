package com.codeclash.authservice.permission.model;

import com.codeclash.authservice.permission.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.permission.model.exception.NullPermissionIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Id {

  public static final int VERSION = 7;

  private final UUID value;

  private Id(UUID value) {
    this.value = value;
  }

  public static Id of(UUID value) {
    if (value == null) throw new NullPermissionIdException();
    if (value.version() != Id.VERSION) throw new InvalidPermissionIdException(value);
    return new Id(value);
  }

  public static Id unsafe(UUID value) {
    return new Id(value);
  }

  public UUID value() {
    return this.value;
  }
}
