package com.codeclash.runnerservice.testcase.model;

import com.codeclash.runnerservice.testcase.model.exception.NullPublicException;

public record Public(Boolean value) {

  public Public {
    if (value == null) throw new NullPublicException();
  }
}
