package com.codeclash.contestservice.user.adapter.config;

import com.codeclash.common.event.RetryProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.user.cdc-consumer")
@Validated
public record UserCdcConsumerProperties(
    @NotBlank String topic, @Positive int concurrency, @Valid @NotNull RetryProperties retry) {}
