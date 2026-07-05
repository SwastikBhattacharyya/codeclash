package com.codeclash.common.permission;

import com.codeclash.common.permission.exception.NullPermissionDescriptionException;
import com.codeclash.common.permission.exception.PermissionDescriptionTooLongException;
import com.codeclash.common.permission.exception.PermissionDescriptionTooShortException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Description {

  public static final int MIN_SIZE = 1;
  public static final int MAX_SIZE = 255;

  private final String value;

  private Description(String value) {
    this.value = value;
  }

  public static Description of(String value) {
    if (value == null) throw new NullPermissionDescriptionException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size < Description.MIN_SIZE) throw new PermissionDescriptionTooShortException(stripped);
    if (size > Description.MAX_SIZE) throw new PermissionDescriptionTooLongException(stripped);
    return new Description(stripped);
  }

  public static Description unsafe(String value) {
    return new Description(value);
  }

  public String value() {
    return this.value;
  }
}
