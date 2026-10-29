package com.codeclash.runnerservice.submission.adapter.config;

import com.codeclash.common.event.TopicProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.submission")
@Validated
public record SubmissionProperties(
    @Valid @NotNull TopicProperties stateTopic,
    @Valid @NotNull Worker worker,
    @NotNull Map<String, @NotNull @Positive Float> timeMultipliers) {

  public record Worker(
      @Positive int maxConcurrent,
      @NotNull Duration lease,
      @Positive int maxAttempts,
      @NotNull Duration pollInterval) {}
}
