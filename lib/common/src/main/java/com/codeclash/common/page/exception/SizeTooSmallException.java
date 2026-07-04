package com.codeclash.common.page.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.page.Size;
import lombok.Getter;

@Getter
public class SizeTooSmallException extends CodedException {

  private final int size;

  public SizeTooSmallException(int size) {
    super(CommonErrorCode.SIZE_TOO_SMALL, "Size must be at least " + Size.MIN);
    this.size = size;
  }
}
