package com.codeclash.problemservice.submission.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Username {

  private final String value;

  private Username(String value) {
    this.value = value;
  }

  public static Username unsafe(String value) {
    return new Username(value);
  }

  public String value() {
    return this.value;
  }
}
