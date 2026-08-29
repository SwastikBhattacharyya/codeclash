package com.codeclash.problemservice.submission.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.submission")
@Validated
public record SubmissionProperties(@Valid @NotNull TopicProperties cdcTopic) {}
