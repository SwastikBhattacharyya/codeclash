package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidUserIdException extends CodedException {

  private final UUID id;

  public InvalidUserIdException(UUID id) {
    super(SubmissionErrorCode.INVALID_USER_ID, "%s is not a valid user id".formatted(id));
    this.id = id;
  }
}
