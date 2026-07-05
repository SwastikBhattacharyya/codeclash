package com.codeclash.common.permission;

import com.codeclash.common.permission.exception.InvalidPermissionNameException;
import com.codeclash.common.permission.exception.NullPermissionNameException;
import com.codeclash.common.permission.exception.PermissionNameTooLongException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Name {

  public static final int MAX_SIZE = 255;
  public static final int SEGMENTS = 3;

  private static final Pattern FORMAT =
      Pattern.compile("[a-z]+(?:_[a-z]+)*(?::[a-z]+(?:_[a-z]+)*){%d}".formatted(Name.SEGMENTS - 1));

  private final String value;

  private Name(String value) {
    this.value = value;
  }

  public static Name of(String value) {
    if (value == null) throw new NullPermissionNameException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size > Name.MAX_SIZE) throw new PermissionNameTooLongException(stripped);
    if (!Name.FORMAT.matcher(stripped).matches())
      throw new InvalidPermissionNameException(stripped);
    return new Name(stripped);
  }

  public static Name unsafe(String value) {
    return new Name(value);
  }

  public String value() {
    return this.value;
  }
}
