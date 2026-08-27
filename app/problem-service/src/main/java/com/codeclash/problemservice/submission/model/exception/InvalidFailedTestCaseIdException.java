package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidFailedTestCaseIdException extends CodedException {

  private final UUID id;

  public InvalidFailedTestCaseIdException(UUID id) {
    super(
        SubmissionErrorCode.INVALID_FAILED_TEST_CASE_ID,
        "%s is not a valid failed test case id".formatted(id));
    this.id = id;
  }
}
