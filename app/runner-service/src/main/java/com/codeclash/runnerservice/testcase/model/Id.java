package com.codeclash.runnerservice.testcase.model;

import com.codeclash.runnerservice.testcase.model.exception.InvalidTestCaseIdException;
import com.codeclash.runnerservice.testcase.model.exception.NullTestCaseIdException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Id {

  public static final int VERSION = 7;

  private final UUID value;

  private Id(UUID value) {
    this.value = value;
  }

  public static Id of(UUID value) {
    if (value == null) throw new NullTestCaseIdException();
    if (value.version() != Id.VERSION) throw new InvalidTestCaseIdException(value);
    return new Id(value);
  }

  public static Id unsafe(UUID value) {
    return new Id(value);
  }

  public UUID value() {
    return this.value;
  }
}
