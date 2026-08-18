package com.codeclash.problemservice.question.api;

import java.time.Instant;
import java.util.UUID;

public record QuestionView(UUID id, boolean visible, Instant createdAt, Instant updatedAt) {}
