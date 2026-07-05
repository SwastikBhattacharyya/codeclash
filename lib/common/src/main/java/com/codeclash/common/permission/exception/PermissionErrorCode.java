package com.codeclash.common.permission.exception;

import com.codeclash.common.exception.ErrorCode;

public enum PermissionErrorCode implements ErrorCode {
  INVALID_PERMISSION_NAME("Invalid permission name"),
  NULL_PERMISSION_DESCRIPTION("Permission description is required"),
  NULL_PERMISSION_NAME("Permission name is required"),
  PERMISSION_DESCRIPTION_TOO_LONG("Permission description too long"),
  PERMISSION_DESCRIPTION_TOO_SHORT("Permission description too short"),
  PERMISSION_NAME_TOO_LONG("Permission name too long");

  private final String title;

  PermissionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
