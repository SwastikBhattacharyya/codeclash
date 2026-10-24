package com.codeclash.runnerservice.testcase.adapter.config;

import com.codeclash.common.event.RetryProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.test-case.cdc-consumer")
@Validated
public record TestCaseCdcConsumerProperties(
    @NotBlank String topic,
    @Valid @NotNull RetryProperties retry,
    @NotNull DataSize deadLetterMaxMessageSize) {}
