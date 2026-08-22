package com.codeclash.problemservice.testcase.model;

import com.codeclash.problemservice.testcase.model.exception.InputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.NullInputException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Input {

  public static final int MAX_SIZE = 1_048_576;

  private final String value;

  private Input(String value) {
    this.value = value;
  }

  public static Input of(String value) {
    if (value == null) throw new NullInputException();
    if (value.codePointCount(0, value.length()) > Input.MAX_SIZE) throw new InputTooLongException();
    return new Input(value);
  }

  public static Input unsafe(String value) {
    return new Input(value);
  }

  public String value() {
    return this.value;
  }
}
