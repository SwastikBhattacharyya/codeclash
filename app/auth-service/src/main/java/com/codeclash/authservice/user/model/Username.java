package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.exception.InvalidUsernameException;
import com.codeclash.authservice.user.model.exception.NullUsernameException;
import com.codeclash.authservice.user.model.exception.UsernameTooLongException;
import com.codeclash.authservice.user.model.exception.UsernameTooShortException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Username {

  public static final int MIN_SIZE = 3;
  public static final int MAX_SIZE = 32;
  public static final String ALLOWED_CHARACTERS = "A-Za-z0-9_-";

  private static final Pattern FORMAT =
      Pattern.compile("[%s]+".formatted(Username.ALLOWED_CHARACTERS));

  private final String value;

  private Username(String value) {
    this.value = value;
  }

  public static Username of(String value) {
    if (value == null) throw new NullUsernameException();
    String stripped = value.strip();
    int size = stripped.codePointCount(0, stripped.length());
    if (size < Username.MIN_SIZE) throw new UsernameTooShortException(stripped);
    if (size > Username.MAX_SIZE) throw new UsernameTooLongException(stripped);
    if (!Username.FORMAT.matcher(stripped).matches()) throw new InvalidUsernameException(stripped);
    return new Username(stripped);
  }

  public static Username unsafe(String value) {
    return new Username(value);
  }

  public String value() {
    return this.value;
  }
}
