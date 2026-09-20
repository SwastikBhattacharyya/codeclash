package com.codeclash.contracts.contest.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContestCdcEventData(
    UUID id,
    Instant liveAt,
    Instant endAt,
    Instant openedAt,
    List<UUID> questionIds,
    Instant createdAt,
    Instant updatedAt) {}
