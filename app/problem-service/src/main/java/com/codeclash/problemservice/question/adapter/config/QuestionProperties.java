package com.codeclash.problemservice.question.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.question")
@Validated
public record QuestionProperties(@Valid @NotNull TopicProperties cdcTopic) {}
