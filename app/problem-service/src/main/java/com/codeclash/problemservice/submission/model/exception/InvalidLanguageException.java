package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidLanguageException extends CodedException {

  private final String value;

  public InvalidLanguageException(String value) {
    super(SubmissionErrorCode.INVALID_LANGUAGE, "%s is not a valid language".formatted(value));
    this.value = value;
  }
}
