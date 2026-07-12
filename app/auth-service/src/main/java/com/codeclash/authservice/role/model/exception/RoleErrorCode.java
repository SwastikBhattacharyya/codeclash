package com.codeclash.authservice.role.model.exception;

import com.codeclash.common.exception.ErrorCode;

public enum RoleErrorCode implements ErrorCode {
  DEFAULT_ROLE_MODIFICATION("Default role cannot be modified"),
  DESCRIPTION_TOO_LONG("Description too long"),
  DESCRIPTION_TOO_SHORT("Description too short"),
  DUPLICATE_ROLE_NAME("Role name already exists"),
  INVALID_NAME("Invalid name"),
  INVALID_PERMISSION_ID("Invalid permission id"),
  INVALID_ROLE_ID("Invalid role id"),
  NAME_TOO_LONG("Name too long"),
  NULL_DESCRIPTION("Description is required"),
  NULL_NAME("Name is required"),
  NULL_PERMISSIONS("Permissions are required"),
  NULL_ROLE_ID("Role id is required"),
  PERMISSIONS_NOT_FOUND("Permissions not found"),
  ROLE_NOT_FOUND("Role not found");

  private final String title;

  RoleErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
