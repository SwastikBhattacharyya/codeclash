package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SubmissionNotFoundException extends CodedException {

  private final UUID id;

  public SubmissionNotFoundException(UUID id) {
    super(
        SubmissionErrorCode.SUBMISSION_NOT_FOUND, "Submission with id %s not found".formatted(id));
    this.id = id;
  }
}
