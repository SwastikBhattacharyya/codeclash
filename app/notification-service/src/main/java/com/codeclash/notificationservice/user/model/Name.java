package com.codeclash.notificationservice.user.model;

import com.codeclash.notificationservice.user.model.exception.NullNameException;

public record Name(String value) {

  public Name {
    if (value == null) throw new NullNameException();
  }
}
