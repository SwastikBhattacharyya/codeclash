package com.codeclash.runnerservice.testcase.model;

import com.codeclash.runnerservice.testcase.model.exception.NullOutputException;

public record Output(String value) {

  public Output {
    if (value == null) throw new NullOutputException();
  }
}
