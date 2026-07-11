package com.codeclash.authservice.permission.api.exception;

import com.codeclash.authservice.permission.model.exception.PermissionErrorCode;
import com.codeclash.common.exception.CodedException;
import java.util.List;
import lombok.Getter;

@Getter
public class PermissionNamesNotFoundException extends CodedException {

  private final List<String> names;

  public PermissionNamesNotFoundException(List<String> names) {
    super(PermissionErrorCode.PERMISSIONS_NOT_FOUND, "Permissions not found: %s".formatted(names));
    this.names = List.copyOf(names);
  }
}
