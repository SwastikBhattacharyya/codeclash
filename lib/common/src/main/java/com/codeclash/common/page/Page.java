package com.codeclash.common.page;

import com.codeclash.common.page.exception.PageTooSmallException;

public record Page(int value) {

  public static final int MIN = 1;
  public static final String DEFAULT = "1";

  public Page {
    if (value < Page.MIN) throw new PageTooSmallException(value);
  }
}
