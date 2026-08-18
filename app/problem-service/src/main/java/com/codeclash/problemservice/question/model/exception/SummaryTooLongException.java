package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Summary;
import lombok.Getter;

@Getter
public class SummaryTooLongException extends CodedException {

  private final String summary;

  public SummaryTooLongException(String summary) {
    super(
        QuestionErrorCode.SUMMARY_TOO_LONG,
        "Summary must contain at most %d characters".formatted(Summary.MAX_SIZE));
    this.summary = summary;
  }
}
