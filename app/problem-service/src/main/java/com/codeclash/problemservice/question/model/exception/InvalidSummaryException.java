package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidSummaryException extends CodedException {

  private final String summary;

  public InvalidSummaryException(String summary) {
    super(QuestionErrorCode.INVALID_SUMMARY, "Summary must not have surrounding whitespace");
    this.summary = summary;
  }
}
