package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.submission.model.CompileOutput;

public class CompileOutputTooLongException extends CodedException {

  public CompileOutputTooLongException() {
    super(
        SubmissionErrorCode.COMPILE_OUTPUT_TOO_LONG,
        "Compile output must contain at most %d characters".formatted(CompileOutput.MAX_SIZE));
  }
}
