package com.codeclash.contracts.problem.event;

import java.time.Instant;
import java.util.UUID;

public record QuestionCdcEventData(
    UUID id,
    boolean visible,
    int maxRunTimeMs,
    int maxRamLimitMb,
    Instant createdAt,
    Instant updatedAt) {}
