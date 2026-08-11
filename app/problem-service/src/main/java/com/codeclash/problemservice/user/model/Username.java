package com.codeclash.problemservice.user.model;

import com.codeclash.problemservice.user.model.exception.NullUsernameException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Username {

  private final String value;

  private Username(String value) {
    this.value = value;
  }

  public static Username of(String value) {
    if (value == null) throw new NullUsernameException();
    return new Username(value);
  }

  public static Username unsafe(String value) {
    return new Username(value);
  }

  public String value() {
    return this.value;
  }
}
