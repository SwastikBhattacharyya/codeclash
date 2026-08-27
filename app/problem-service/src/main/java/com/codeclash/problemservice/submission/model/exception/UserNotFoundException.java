package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserNotFoundException extends CodedException {

  private final UUID id;

  public UserNotFoundException(UUID id) {
    super(SubmissionErrorCode.USER_NOT_FOUND, "User with id %s not found".formatted(id));
    this.id = id;
  }
}
