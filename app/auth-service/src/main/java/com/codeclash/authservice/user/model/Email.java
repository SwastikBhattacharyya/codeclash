package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.exception.EmailTooLongException;
import com.codeclash.authservice.user.model.exception.InvalidEmailException;
import com.codeclash.authservice.user.model.exception.NullEmailException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Email {

  public static final int MAX_SIZE = 254;

  private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+$");

  private final String value;

  private Email(String value) {
    this.value = value;
  }

  public static Email of(String value) {
    if (value == null) throw new NullEmailException();
    String stripped = value.strip();
    if (stripped.codePointCount(0, stripped.length()) > Email.MAX_SIZE)
      throw new EmailTooLongException(stripped);
    if (!Email.FORMAT.matcher(stripped).matches()) throw new InvalidEmailException(stripped);
    return new Email(stripped);
  }

  public static Email unsafe(String value) {
    return new Email(value);
  }

  public String value() {
    return this.value;
  }
}
