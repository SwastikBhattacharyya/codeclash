package com.codeclash.problemservice.question.model;

import com.codeclash.problemservice.question.model.exception.InvalidTitleException;
import com.codeclash.problemservice.question.model.exception.NullTitleException;
import com.codeclash.problemservice.question.model.exception.TitleTooLongException;
import com.codeclash.problemservice.question.model.exception.TitleTooShortException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Title {

  public static final int MIN_SIZE = 8;
  public static final int MAX_SIZE = 64;

  private static final Pattern LINE_BREAK = Pattern.compile("[\\n\\r\\u0085\\u2028\\u2029]");

  private final String value;

  private Title(String value) {
    this.value = value;
  }

  public static Title of(String value) {
    if (value == null) throw new NullTitleException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size < Title.MIN_SIZE) throw new TitleTooShortException(stripped);
    if (size > Title.MAX_SIZE) throw new TitleTooLongException(stripped);
    if (Title.LINE_BREAK.matcher(stripped).find()) throw new InvalidTitleException(stripped);
    return new Title(stripped);
  }

  public static Title unsafe(String value) {
    return new Title(value);
  }

  public String value() {
    return this.value;
  }
}
