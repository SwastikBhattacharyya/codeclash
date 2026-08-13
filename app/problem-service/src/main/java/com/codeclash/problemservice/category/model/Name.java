package com.codeclash.problemservice.category.model;

import com.codeclash.problemservice.category.model.exception.InvalidNameException;
import com.codeclash.problemservice.category.model.exception.NameTooLongException;
import com.codeclash.problemservice.category.model.exception.NameTooShortException;
import com.codeclash.problemservice.category.model.exception.NullNameException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Name {

  public static final int MIN_SIZE = 2;
  public static final int MAX_SIZE = 64;

  private static final Pattern LINE_BREAK = Pattern.compile("[\\n\\r\\u0085\\u2028\\u2029]");

  private final String value;

  private Name(String value) {
    this.value = value;
  }

  public static Name of(String value) {
    if (value == null) throw new NullNameException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size < Name.MIN_SIZE) throw new NameTooShortException(stripped);
    if (size > Name.MAX_SIZE) throw new NameTooLongException(stripped);
    if (Name.LINE_BREAK.matcher(stripped).find()) throw new InvalidNameException(stripped);
    return new Name(stripped);
  }

  public static Name unsafe(String value) {
    return new Name(value);
  }

  public String value() {
    return this.value;
  }
}
