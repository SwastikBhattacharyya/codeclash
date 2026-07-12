package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PermissionsNotFoundException extends CodedException {

  private final List<UUID> ids;

  public PermissionsNotFoundException(List<UUID> ids) {
    super(RoleErrorCode.PERMISSIONS_NOT_FOUND, "Permissions not found: %s".formatted(ids));
    this.ids = List.copyOf(ids);
  }
}
