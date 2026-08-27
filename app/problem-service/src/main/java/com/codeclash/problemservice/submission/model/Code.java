package com.codeclash.problemservice.submission.model;

import com.codeclash.problemservice.submission.model.exception.BlankCodeException;
import com.codeclash.problemservice.submission.model.exception.CodeTooLongException;
import com.codeclash.problemservice.submission.model.exception.NullCodeException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Code {

  public static final int MAX_SIZE = 65_536;

  private final String value;

  private Code(String value) {
    this.value = value;
  }

  public static Code of(String value) {
    if (value == null) throw new NullCodeException();
    if (value.codePointCount(0, value.length()) > Code.MAX_SIZE) throw new CodeTooLongException();
    if (value.isBlank()) throw new BlankCodeException();
    return new Code(value);
  }

  public static Code unsafe(String value) {
    return new Code(value);
  }

  public String value() {
    return this.value;
  }
}
