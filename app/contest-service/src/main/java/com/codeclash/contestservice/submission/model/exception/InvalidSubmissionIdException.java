package com.codeclash.contestservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidSubmissionIdException extends CodedException {

  private final UUID id;

  public InvalidSubmissionIdException(UUID id) {
    super(SubmissionErrorCode.INVALID_SUBMISSION_ID, "%s is not a valid submission id".formatted(id));
    this.id = id;
  }
}
