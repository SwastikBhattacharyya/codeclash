package com.codeclash.runnerservice.submission.model;

import com.codeclash.runnerservice.submission.model.exception.NullCodeException;

public record Code(String value) {

  public Code {
    if (value == null) throw new NullCodeException();
  }
}
