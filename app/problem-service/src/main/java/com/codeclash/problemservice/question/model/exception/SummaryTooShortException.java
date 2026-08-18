package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Summary;
import lombok.Getter;

@Getter
public class SummaryTooShortException extends CodedException {

  private final String summary;

  public SummaryTooShortException(String summary) {
    super(
        QuestionErrorCode.SUMMARY_TOO_SHORT,
        "Summary must contain at least %d characters".formatted(Summary.MIN_SIZE));
    this.summary = summary;
  }
}
