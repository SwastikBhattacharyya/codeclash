package com.codeclash.common.page.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.page.Page;
import lombok.Getter;

@Getter
public class PageTooSmallException extends CodedException {

  private final int page;

  public PageTooSmallException(int page) {
    super(CommonErrorCode.PAGE_TOO_SMALL, "Page must be at least " + Page.MIN);
    this.page = page;
  }
}
