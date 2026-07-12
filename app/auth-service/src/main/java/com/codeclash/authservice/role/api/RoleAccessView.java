package com.codeclash.authservice.role.api;

import java.util.List;
import java.util.UUID;

public record RoleAccessView(UUID id, String name, List<String> permissions) {}
