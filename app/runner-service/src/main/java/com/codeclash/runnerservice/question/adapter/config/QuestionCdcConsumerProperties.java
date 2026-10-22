package com.codeclash.runnerservice.question.adapter.config;

import com.codeclash.common.event.RetryProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.question.cdc-consumer")
@Validated
public record QuestionCdcConsumerProperties(
    @NotBlank String topic, @Valid @NotNull RetryProperties retry) {}
