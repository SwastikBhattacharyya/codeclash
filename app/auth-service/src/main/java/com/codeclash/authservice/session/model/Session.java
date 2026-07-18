package com.codeclash.authservice.session.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;

public record Session(
    Id id, UserId userId, LastUsedAt lastUsedAt, CreatedAt createdAt, UpdatedAt updatedAt) {}
