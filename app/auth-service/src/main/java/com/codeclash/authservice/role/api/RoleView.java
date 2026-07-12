package com.codeclash.authservice.role.api;

import java.time.Instant;
import java.util.UUID;

public record RoleView(
    UUID id,
    String name,
    String description,
    boolean isDefault,
    Instant createdAt,
    Instant updatedAt) {}
