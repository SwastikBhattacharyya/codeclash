package com.codeclash.problemservice.testcase.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.test-case")
@Validated
public record TestCaseProperties(
    @Valid @NotNull TopicProperties cdcTopic, @NotNull DataSize cdcTopicMaxMessageSize) {}
