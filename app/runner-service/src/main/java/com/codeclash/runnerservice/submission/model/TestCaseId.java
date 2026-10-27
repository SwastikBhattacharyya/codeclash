package com.codeclash.runnerservice.submission.model;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class TestCaseId {

  private final UUID value;

  private TestCaseId(UUID value) {
    this.value = value;
  }

  public static TestCaseId unsafe(UUID value) {
    return new TestCaseId(value);
  }

  public UUID value() {
    return this.value;
  }
}
