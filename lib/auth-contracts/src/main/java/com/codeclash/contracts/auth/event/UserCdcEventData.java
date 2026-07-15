package com.codeclash.contracts.auth.event;

import java.time.Instant;
import java.util.UUID;

public record UserCdcEventData(
    UUID id,
    String name,
    String username,
    String email,
    boolean verified,
    UUID verificationToken,
    UUID roleId,
    Instant createdAt,
    Instant updatedAt) {}
