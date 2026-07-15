package com.codeclash.authservice.user.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.user")
@Validated
public record UserProperties(
    @NotBlank String defaultRole, @Valid @NotNull TopicProperties cdcTopic) {}
