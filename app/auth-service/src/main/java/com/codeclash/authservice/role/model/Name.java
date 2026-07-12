package com.codeclash.authservice.role.model;

import com.codeclash.authservice.role.model.exception.InvalidNameException;
import com.codeclash.authservice.role.model.exception.NameTooLongException;
import com.codeclash.authservice.role.model.exception.NullNameException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Name {

  public static final int MAX_SIZE = 255;

  private static final Pattern FORMAT = Pattern.compile("[a-z]+(?:_[a-z]+)*");

  private final String value;

  private Name(String value) {
    this.value = value;
  }

  public static Name of(String value) {
    if (value == null) throw new NullNameException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size > Name.MAX_SIZE) throw new NameTooLongException(stripped);
    if (!Name.FORMAT.matcher(stripped).matches()) throw new InvalidNameException(stripped);
    return new Name(stripped);
  }

  public static Name unsafe(String value) {
    return new Name(value);
  }

  public String value() {
    return this.value;
  }
}
