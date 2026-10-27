package com.codeclash.runnerservice.submission.model;

import com.codeclash.contracts.problem.submission.Language;
import java.time.Duration;
import java.util.Map;

public record JudgeSettings(Duration lease, int maxAttempts, Map<String, Float> timeMultipliers) {

  public static final float DEFAULT_TIME_MULTIPLIER = 1.0f;

  public int runTimeLimitMs(Language language, int maxRunTimeMs) {
    float multiplier =
        this.timeMultipliers.getOrDefault(language.value(), JudgeSettings.DEFAULT_TIME_MULTIPLIER);
    return (int) Math.ceil(maxRunTimeMs * multiplier);
  }
}
