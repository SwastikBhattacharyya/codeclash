package com.codeclash.contracts.problem.submission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum SubmissionState {
  QUEUED("queued"),
  COMPILED("compiled"),
  COMPILATION_ERROR("compilation_error"),
  CORRECT("correct"),
  INCORRECT("incorrect"),
  RUNTIME_ERROR("runtime_error"),
  TIME_LIMIT_EXCEEDED("time_limit_exceeded"),
  MEMORY_LIMIT_EXCEEDED("memory_limit_exceeded"),
  RUNNER_ERROR("runner_error");

  private final String value;

  SubmissionState(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  public static Optional<SubmissionState> find(String value) {
    return Arrays.stream(SubmissionState.values())
        .filter(state -> state.value.equals(value))
        .findFirst();
  }

  @JsonCreator
  public static SubmissionState from(String value) {
    return SubmissionState.find(value)
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown SubmissionState: %s".formatted(value)));
  }

  public static List<String> allowed() {
    return Arrays.stream(SubmissionState.values()).map(SubmissionState::value).toList();
  }
}
