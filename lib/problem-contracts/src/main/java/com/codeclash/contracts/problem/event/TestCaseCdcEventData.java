package com.codeclash.contracts.problem.event;

import java.time.Instant;
import java.util.UUID;

public record TestCaseCdcEventData(
    UUID id,
    String input,
    String output,
    boolean isPublic,
    UUID questionId,
    Instant createdAt,
    Instant updatedAt) {}
