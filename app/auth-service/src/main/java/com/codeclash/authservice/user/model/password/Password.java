package com.codeclash.authservice.user.model.password;

import com.codeclash.authservice.user.model.exception.InvalidPasswordHashException;
import com.codeclash.authservice.user.model.exception.NullPasswordException;
import com.codeclash.authservice.user.model.exception.PasswordMissingDigitException;
import com.codeclash.authservice.user.model.exception.PasswordMissingLowercaseException;
import com.codeclash.authservice.user.model.exception.PasswordMissingSpecialCharacterException;
import com.codeclash.authservice.user.model.exception.PasswordMissingUppercaseException;
import com.codeclash.authservice.user.model.exception.PasswordTooLongException;
import com.codeclash.authservice.user.model.exception.PasswordTooShortException;
import java.text.Normalizer;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class Password<S extends PasswordState> {

  public static final int MIN_SIZE = 8;
  public static final int MAX_SIZE = 256;

  private final String value;

  private Password(String value) {
    this.value = value;
  }

  public static Password<Raw> raw(String value) {
    if (value == null) throw new NullPasswordException();
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
    int length = normalized.codePointCount(0, normalized.length());
    if (length < Password.MIN_SIZE) throw new PasswordTooShortException();
    if (length > Password.MAX_SIZE) throw new PasswordTooLongException();
    if (normalized.codePoints().noneMatch(Character::isLowerCase))
      throw new PasswordMissingLowercaseException();
    if (normalized.codePoints().noneMatch(Character::isUpperCase))
      throw new PasswordMissingUppercaseException();
    if (normalized.codePoints().noneMatch(Character::isDigit))
      throw new PasswordMissingDigitException();
    if (normalized.codePoints().noneMatch(Password::isSpecial))
      throw new PasswordMissingSpecialCharacterException();
    return new Password<>(normalized);
  }

  public static Password<Raw> candidate(String value) {
    if (value == null) throw new NullPasswordException();
    return new Password<>(Normalizer.normalize(value, Normalizer.Form.NFKC));
  }

  public static Password<Hashed> hashed(String value) {
    if (value == null) throw new NullPasswordException();
    if (value.isBlank()) throw new InvalidPasswordHashException();
    return new Password<>(value);
  }

  public static Password<Hashed> unsafeHashed(String value) {
    return new Password<>(value);
  }

  public String value() {
    return this.value;
  }

  private static boolean isSpecial(int codePoint) {
    int type = Character.getType(codePoint);
    return !Character.isLetterOrDigit(codePoint)
        && !Character.isWhitespace(codePoint)
        && !Character.isSpaceChar(codePoint)
        && type != Character.CONTROL
        && type != Character.FORMAT;
  }
}
