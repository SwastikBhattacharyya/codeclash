package com.codeclash.authservice.permission.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum PermissionErrorCode implements ErrorCode {
  DUPLICATE_PERMISSION_NAME("Permission name already defined"),
  INVALID_PERMISSION_ID("Invalid permission id"),
  NULL_PERMISSION_ID("Permission id is required"),
  PERMISSION_NOT_FOUND("Permission not found"),
  PERMISSIONS_NOT_FOUND("Permissions not found");

  private final String title;

  PermissionErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
