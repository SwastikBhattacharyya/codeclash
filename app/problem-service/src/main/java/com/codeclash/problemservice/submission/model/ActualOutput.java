package com.codeclash.problemservice.submission.model;

import com.codeclash.problemservice.submission.model.exception.ActualOutputTooLongException;
import com.codeclash.problemservice.submission.model.exception.NullActualOutputException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class ActualOutput {

  public static final int MAX_SIZE = 65_536;

  private final String value;

  private ActualOutput(String value) {
    this.value = value;
  }

  public static ActualOutput of(String value) {
    if (value == null) throw new NullActualOutputException();
    if (value.codePointCount(0, value.length()) > ActualOutput.MAX_SIZE)
      throw new ActualOutputTooLongException();
    return new ActualOutput(value);
  }

  public static ActualOutput unsafe(String value) {
    return new ActualOutput(value);
  }

  public String value() {
    return this.value;
  }
}
