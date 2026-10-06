package com.codeclash.problemservice.contest.api.event;

import java.util.List;
import java.util.UUID;

public record ContestOpenedEvent(UUID contestId, List<UUID> questionIds) {}
