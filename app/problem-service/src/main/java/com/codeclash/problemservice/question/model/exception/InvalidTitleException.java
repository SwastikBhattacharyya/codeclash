package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import lombok.Getter;

@Getter
public class InvalidTitleException extends CodedException {

  private final String title;

  public InvalidTitleException(String title) {
    super(
        QuestionErrorCode.INVALID_TITLE,
        "Title must be a single line without surrounding whitespace");
    this.title = title;
  }
}
