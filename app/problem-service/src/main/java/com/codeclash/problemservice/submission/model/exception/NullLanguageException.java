package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullLanguageException extends CodedException {

  public NullLanguageException() {
    super(SubmissionErrorCode.NULL_LANGUAGE, "Language cannot be null");
  }
}
