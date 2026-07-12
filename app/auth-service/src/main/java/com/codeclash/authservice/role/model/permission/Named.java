package com.codeclash.authservice.role.model.permission;

import com.codeclash.common.permission.Name;
import java.util.List;

public record Named(List<Name> names) implements PermissionsState {}
