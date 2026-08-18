package com.codeclash.problemservice.question.model.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.problemservice.question.model.Title;
import lombok.Getter;

@Getter
public class TitleTooLongException extends CodedException {

  private final String title;

  public TitleTooLongException(String title) {
    super(
        QuestionErrorCode.TITLE_TOO_LONG,
        "Title must contain at most %d characters".formatted(Title.MAX_SIZE));
    this.title = title;
  }
}
