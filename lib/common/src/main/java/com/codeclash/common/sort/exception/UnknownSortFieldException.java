package com.codeclash.common.sort.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import java.util.List;
import lombok.Getter;

@Getter
public class UnknownSortFieldException extends CodedException {

  private final String field;
  private final List<String> allowed;

  public UnknownSortFieldException(String field, List<String> allowed) {
    super(CommonErrorCode.UNKNOWN_SORT_FIELD, "Sort field does not exist");
    this.field = field;
    this.allowed = allowed;
  }
}
