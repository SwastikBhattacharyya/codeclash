package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.submission.model.Code;

public class CodeTooLongException extends CodedException {

  public CodeTooLongException() {
    super(
        SubmissionErrorCode.CODE_TOO_LONG,
        "Code must contain at most %d characters".formatted(Code.MAX_SIZE));
  }
}
