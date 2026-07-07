package com.codeclash.contracts.auth;

import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import com.codeclash.common.permission.PermissionList;
import java.util.List;

public final class AuthPermissions implements PermissionList {

  public static final String PERMISSION_READ = "auth:permission:read";
  public static final String ROLE_READ = "auth:role:read";
  public static final String ROLE_WRITE = "auth:role:write";
  public static final String SESSION_READ = "auth:session:read";
  public static final String SESSION_READ_OWN = "auth:session:read_own";
  public static final String SESSION_WRITE = "auth:session:write";
  public static final String SESSION_WRITE_OWN = "auth:session:write_own";
  public static final String USER_WRITE = "auth:user:write";
  public static final String USER_WRITE_OWN = "auth:user:write_own";
  public static final String USER_WRITE_ROLE = "auth:user:write_role";
  public static final String USER_WRITE_ROLE_OWN = "auth:user:write_role_own";
  public static final String USER_WRITE_IS_VERIFIED = "auth:user:write_is_verified";
  public static final String USER_WRITE_IS_VERIFIED_OWN = "auth:user:write_is_verified_own";
  public static final String USER_WRITE_USERNAME = "auth:user:write_username";
  public static final String USER_WRITE_USERNAME_OWN = "auth:user:write_username_own";
  public static final String USER_READ_EMAIL = "auth:user:read_email";
  public static final String USER_READ_EMAIL_OWN = "auth:user:read_email_own";
  public static final String USER_READ_ROLE = "auth:user:read_role";
  public static final String USER_READ_ROLE_OWN = "auth:user:read_role_own";

  @Override
  public List<PermissionDefinition> permissions() {
    return List.of(
        AuthPermissions.definition(AuthPermissions.PERMISSION_READ, "Read permissions"),
        AuthPermissions.definition(AuthPermissions.ROLE_READ, "Read roles"),
        AuthPermissions.definition(
            AuthPermissions.ROLE_WRITE, "Create, replace, patch and delete roles"),
        AuthPermissions.definition(AuthPermissions.SESSION_READ, "Read other users' sessions"),
        AuthPermissions.definition(AuthPermissions.SESSION_READ_OWN, "Read your own sessions"),
        AuthPermissions.definition(AuthPermissions.SESSION_WRITE, "Delete other users' sessions"),
        AuthPermissions.definition(AuthPermissions.SESSION_WRITE_OWN, "Delete your own sessions"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE, "Create, replace, patch and delete other users"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE_OWN, "Replace, patch and delete your own user"),
        AuthPermissions.definition(AuthPermissions.USER_WRITE_ROLE, "Set other users' roles"),
        AuthPermissions.definition(AuthPermissions.USER_WRITE_ROLE_OWN, "Change your own role"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE_IS_VERIFIED, "Set whether other users are verified"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE_IS_VERIFIED_OWN, "Change whether you are verified"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE_USERNAME, "Change other users' usernames"),
        AuthPermissions.definition(
            AuthPermissions.USER_WRITE_USERNAME_OWN, "Change your own username"),
        AuthPermissions.definition(AuthPermissions.USER_READ_EMAIL, "Read other users' emails"),
        AuthPermissions.definition(AuthPermissions.USER_READ_EMAIL_OWN, "Read your own email"),
        AuthPermissions.definition(AuthPermissions.USER_READ_ROLE, "Read other users' roles"),
        AuthPermissions.definition(AuthPermissions.USER_READ_ROLE_OWN, "Read your own role"));
  }

  private static PermissionDefinition definition(String name, String description) {
    return new PermissionDefinition(Name.of(name), Description.of(description));
  }
}
