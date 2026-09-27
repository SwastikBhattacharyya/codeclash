package com.codeclash.contestservice.contest.adapter.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMin;

public record ContestOpenerProperties(
    @NotNull @DurationMin(nanos = 0) Duration lead,
    @NotNull @DurationMin(millis = 1) Duration interval,
    @Positive int batchSize) {}
