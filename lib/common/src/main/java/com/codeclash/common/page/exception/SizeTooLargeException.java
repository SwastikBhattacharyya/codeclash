package com.codeclash.common.page.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.page.Size;
import lombok.Getter;

@Getter
public class SizeTooLargeException extends CodedException {

  private final int size;

  public SizeTooLargeException(int size) {
    super(CommonErrorCode.SIZE_TOO_LARGE, "Size must be at most " + Size.MAX);
    this.size = size;
  }
}
