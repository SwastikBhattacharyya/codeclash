package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Title;
import lombok.Getter;

@Getter
public class TitleTooShortException extends CodedException {

  private final String title;

  public TitleTooShortException(String title) {
    super(
        QuestionErrorCode.TITLE_TOO_SHORT,
        "Title must contain at least %d characters".formatted(Title.MIN_SIZE));
    this.title = title;
  }
}
