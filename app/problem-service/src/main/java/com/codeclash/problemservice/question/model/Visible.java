package com.codeclash.problemservice.question.model;

import com.codeclash.problemservice.question.model.exception.NullVisibleException;

public record Visible(Boolean value) {

  public Visible {
    if (value == null) throw new NullVisibleException();
  }
}
