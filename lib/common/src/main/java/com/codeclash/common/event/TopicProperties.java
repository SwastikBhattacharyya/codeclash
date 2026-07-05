package com.codeclash.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TopicProperties(@NotBlank String name, @Positive int partitions) {}
