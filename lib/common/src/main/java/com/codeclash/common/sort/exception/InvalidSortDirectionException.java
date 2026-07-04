package com.codeclash.common.sort.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import lombok.Getter;

@Getter
public class InvalidSortDirectionException extends CodedException {

  private final String direction;

  public InvalidSortDirectionException(String direction) {
    super(CommonErrorCode.INVALID_SORT_DIRECTION, "Sort direction must be asc or desc");
    this.direction = direction;
  }
}
