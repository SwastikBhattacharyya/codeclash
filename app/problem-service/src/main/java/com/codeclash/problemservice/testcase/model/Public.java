package com.codeclash.problemservice.testcase.model;

import com.codeclash.problemservice.testcase.model.exception.NullPublicException;

public record Public(Boolean value) {

  public Public {
    if (value == null) throw new NullPublicException();
  }
}
