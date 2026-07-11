package com.codeclash.authservice.permission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PermissionNotFoundException extends CodedException {

  private final UUID id;

  public PermissionNotFoundException(UUID id) {
    super(
        PermissionErrorCode.PERMISSION_NOT_FOUND, "Permission with id %s not found".formatted(id));
    this.id = id;
  }
}
