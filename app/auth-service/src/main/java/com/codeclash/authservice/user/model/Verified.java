package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.exception.NullVerifiedException;

public record Verified(Boolean value) {

  public Verified {
    if (value == null) throw new NullVerifiedException();
  }
}
