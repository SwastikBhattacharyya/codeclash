package com.codeclash.problemservice.submission.model;

import com.codeclash.problemservice.submission.model.exception.InvalidFailedTestCaseIdException;
import com.codeclash.problemservice.submission.model.exception.NullFailedTestCaseIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class FailedTestCaseId {

  public static final int VERSION = 7;

  private final UUID value;

  private FailedTestCaseId(UUID value) {
    this.value = value;
  }

  public static FailedTestCaseId of(UUID value) {
    if (value == null) throw new NullFailedTestCaseIdException();
    if (value.version() != FailedTestCaseId.VERSION)
      throw new InvalidFailedTestCaseIdException(value);
    return new FailedTestCaseId(value);
  }

  public static FailedTestCaseId unsafe(UUID value) {
    return new FailedTestCaseId(value);
  }

  public UUID value() {
    return this.value;
  }
}
