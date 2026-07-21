package com.codeclash.notificationservice.user.model;

import com.codeclash.notificationservice.user.model.exception.InvalidVerificationTokenException;
import com.codeclash.notificationservice.user.model.exception.NullVerificationTokenException;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class VerificationToken {

  public static final int VERSION = 4;

  private final UUID value;

  private VerificationToken(UUID value) {
    this.value = value;
  }

  public static VerificationToken of(UUID value) {
    if (value == null) throw new NullVerificationTokenException();
    if (value.version() != VerificationToken.VERSION)
      throw new InvalidVerificationTokenException(value);
    return new VerificationToken(value);
  }

  public UUID value() {
    return this.value;
  }
}
