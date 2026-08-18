package com.codeclash.problemservice.question.model;

import com.codeclash.problemservice.question.model.exception.NullSummaryException;
import com.codeclash.problemservice.question.model.exception.SummaryTooLongException;
import com.codeclash.problemservice.question.model.exception.SummaryTooShortException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Summary {

  public static final int MIN_SIZE = 16;
  public static final int MAX_SIZE = 256;

  private final String value;

  private Summary(String value) {
    this.value = value;
  }

  public static Summary of(String value) {
    if (value == null) throw new NullSummaryException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size < Summary.MIN_SIZE) throw new SummaryTooShortException(stripped);
    if (size > Summary.MAX_SIZE) throw new SummaryTooLongException(stripped);
    return new Summary(stripped);
  }

  public static Summary unsafe(String value) {
    return new Summary(value);
  }

  public String value() {
    return this.value;
  }
}
