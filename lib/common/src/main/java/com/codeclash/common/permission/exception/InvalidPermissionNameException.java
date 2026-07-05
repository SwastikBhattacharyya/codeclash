package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.permission.Name;
import lombok.Getter;

@Getter
public class InvalidPermissionNameException extends CodedException {

  private final String name;

  public InvalidPermissionNameException(String name) {
    super(
        PermissionErrorCode.INVALID_PERMISSION_NAME,
        ("Permission name %s must have %d colon-separated segments"
                + " of lowercase letters and underscores")
            .formatted(name, Name.SEGMENTS));
    this.name = name;
  }
}
