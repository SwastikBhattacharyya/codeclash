package com.codeclash.problemservice.submission.model;

import com.codeclash.problemservice.submission.model.exception.CompileOutputTooLongException;
import com.codeclash.problemservice.submission.model.exception.NullCompileOutputException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class CompileOutput {

  public static final int MAX_SIZE = 65_536;

  private final String value;

  private CompileOutput(String value) {
    this.value = value;
  }

  public static CompileOutput of(String value) {
    if (value == null) throw new NullCompileOutputException();
    if (value.codePointCount(0, value.length()) > CompileOutput.MAX_SIZE)
      throw new CompileOutputTooLongException();
    return new CompileOutput(value);
  }

  public static CompileOutput unsafe(String value) {
    return new CompileOutput(value);
  }

  public String value() {
    return this.value;
  }
}
