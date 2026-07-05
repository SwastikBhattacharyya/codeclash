package com.codeclash.common.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

public record RetryProperties(
    @Positive int maxAttempts,
    @NotNull Duration initialInterval,
    @Positive double multiplier,
    @NotNull Duration maxInterval) {}
