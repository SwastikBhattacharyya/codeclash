package com.codeclash.contracts.auth.session.exception;

import com.codeclash.common.exception.CodedException;
import java.util.List;
import lombok.Getter;

@Getter
public class ForbiddenException extends CodedException {

  private final List<String> permissions;

  public ForbiddenException(List<String> permissions) {
    super(AuthErrorCode.FORBIDDEN, "You are not allowed to perform this action");
    this.permissions = List.copyOf(permissions);
  }
}
