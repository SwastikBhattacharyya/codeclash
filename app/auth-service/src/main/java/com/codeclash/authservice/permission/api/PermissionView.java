package com.codeclash.authservice.permission.api;

import java.time.Instant;
import java.util.UUID;

public record PermissionView(
    UUID id, String name, String description, Instant createdAt, Instant updatedAt) {}
