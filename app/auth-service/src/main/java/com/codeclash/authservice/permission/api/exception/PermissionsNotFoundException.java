package com.codeclash.authservice.permission.api.exception;

import com.codeclash.authservice.permission.model.exception.PermissionErrorCode;
import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PermissionsNotFoundException extends CodedException {

  private final List<UUID> ids;

  public PermissionsNotFoundException(List<UUID> ids) {
    super(PermissionErrorCode.PERMISSIONS_NOT_FOUND, "Permissions not found: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
