package com.codeclash.authservice.role.model.permission;

import java.util.List;

public record Unresolved(List<PermissionId> ids) implements PermissionsState {}
