package com.codeclash.notificationservice.user.model;

import com.codeclash.notificationservice.user.model.exception.NullVerifiedException;

public record Verified(Boolean value) {

  public Verified {
    if (value == null) throw new NullVerifiedException();
  }
}
