package com.codeclash.runnerservice.submission.model;

import java.util.Arrays;

public enum JobStatus {
  QUEUED("queued"),
  RUNNING("running"),
  DONE("done");

  private final String value;

  JobStatus(String value) {
    this.value = value;
  }

  public static JobStatus from(String value) {
    return Arrays.stream(JobStatus.values())
        .filter(status -> status.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown JobStatus: %s".formatted(value)));
  }

  public String value() {
    return this.value;
  }
}
