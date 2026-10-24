package com.codeclash.runnerservice.sandbox.adapter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.sandbox")
@Validated
public record SandboxProperties(
    @NotBlank String image,
    @NotBlank String instanceLabel,
    @PositiveOrZero int warmSize,
    @NotNull Duration compileTimeout,
    @NotNull DataSize memory,
    @Positive int pidsLimit,
    @Positive double cpus,
    @NotNull DataSize tmpfsSize,
    @NotNull Duration dockerTimeoutSlack) {}
