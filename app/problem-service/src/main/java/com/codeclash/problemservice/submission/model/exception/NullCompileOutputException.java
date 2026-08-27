package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullCompileOutputException extends CodedException {

  public NullCompileOutputException() {
    super(SubmissionErrorCode.NULL_COMPILE_OUTPUT, "Compile output cannot be null");
  }
}
