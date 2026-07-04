package com.codeclash.common.page;

import com.codeclash.common.page.exception.SizeTooLargeException;
import com.codeclash.common.page.exception.SizeTooSmallException;

public record Size(int value) {

  public static final int MIN = 1;
  public static final int MAX = 100;
  public static final String DEFAULT = "10";

  public Size {
    if (value < Size.MIN) throw new SizeTooSmallException(value);
    if (value > Size.MAX) throw new SizeTooLargeException(value);
  }
}
