package com.codeclash.contestservice.contest.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.contest")
@Validated
public record ContestProperties(
    @Valid @NotNull TopicProperties cdcTopic, @Valid @NotNull ContestOpenerProperties opener) {}
