package com.codeclash.authservice.permission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidPermissionIdException extends CodedException {

  private final UUID id;

  public InvalidPermissionIdException(UUID id) {
    super(
        PermissionErrorCode.INVALID_PERMISSION_ID, "%s is not a valid permission id".formatted(id));
    this.id = id;
  }
}
