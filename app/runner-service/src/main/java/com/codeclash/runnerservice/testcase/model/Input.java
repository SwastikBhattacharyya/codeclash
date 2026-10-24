package com.codeclash.runnerservice.testcase.model;

import com.codeclash.runnerservice.testcase.model.exception.NullInputException;

public record Input(String value) {

  public Input {
    if (value == null) throw new NullInputException();
  }
}
