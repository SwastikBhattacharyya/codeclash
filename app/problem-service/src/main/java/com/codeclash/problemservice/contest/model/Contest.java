package com.codeclash.problemservice.contest.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Contest(
    Id id,
    Instant liveAt,
    Instant endAt,
    Instant openedAt,
    List<UUID> questionIds,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}
