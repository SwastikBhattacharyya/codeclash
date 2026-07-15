package com.codeclash.authservice.user.adapter.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.user.password.argon2")
@Validated
record Argon2Properties(
    @Min(16) int saltLength,
    @Min(32) int hashLength,
    @Min(1) int parallelism,
    @Min(19_456) int memoryKib,
    @Min(2) int iterations) {}
