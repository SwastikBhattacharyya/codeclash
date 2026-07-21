package com.codeclash.notificationservice.user.model;

import com.codeclash.notificationservice.user.model.exception.NullEmailException;

public record Email(String value) {

  public Email {
    if (value == null) throw new NullEmailException();
  }
}
