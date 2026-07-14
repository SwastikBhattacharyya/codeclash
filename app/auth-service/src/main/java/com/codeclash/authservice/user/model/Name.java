package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.exception.InvalidNameException;
import com.codeclash.authservice.user.model.exception.NameTooLongException;
import com.codeclash.authservice.user.model.exception.NameTooShortException;
import com.codeclash.authservice.user.model.exception.NullNameException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Name {

  public static final int MIN_SIZE = 2;
  public static final int MAX_SIZE = 255;
  public static final int MIN_LETTERS = 2;

  private static final Pattern LETTERS =
      Pattern.compile("(?:.*[a-zA-Z]){%d}.*".formatted(Name.MIN_LETTERS));

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
    if (!Name.LETTERS.matcher(stripped).matches()) throw new InvalidNameException(stripped);
    return new Name(stripped);
  }

  public static Name unsafe(String value) {
    return new Name(value);
  }

  public String value() {
    return this.value;
  }
}
