package com.codeclash.problemservice.testcase.model;

import com.codeclash.problemservice.testcase.model.exception.NullOutputException;
import com.codeclash.problemservice.testcase.model.exception.OutputTooLongException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Output {

  public static final int MAX_SIZE = 1_048_576;

  private final String value;

  private Output(String value) {
    this.value = value;
  }

  public static Output of(String value) {
    if (value == null) throw new NullOutputException();
    if (value.codePointCount(0, value.length()) > Output.MAX_SIZE)
      throw new OutputTooLongException();
    return new Output(value);
  }

  public static Output unsafe(String value) {
    return new Output(value);
  }

  public String value() {
    return this.value;
  }
}
