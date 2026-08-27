package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.submission.model.ActualOutput;

public class ActualOutputTooLongException extends CodedException {

  public ActualOutputTooLongException() {
    super(
        SubmissionErrorCode.ACTUAL_OUTPUT_TOO_LONG,
        "Actual output must contain at most %d characters".formatted(ActualOutput.MAX_SIZE));
  }
}
