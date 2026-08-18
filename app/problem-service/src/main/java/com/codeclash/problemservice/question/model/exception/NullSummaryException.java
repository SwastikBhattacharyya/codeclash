package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullSummaryException extends CodedException {

  public NullSummaryException() {
    super(QuestionErrorCode.NULL_SUMMARY, "Summary cannot be null");
  }
}
