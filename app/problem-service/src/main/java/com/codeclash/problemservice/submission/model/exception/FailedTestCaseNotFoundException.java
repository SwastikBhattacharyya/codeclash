package com.codeclash.problemservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FailedTestCaseNotFoundException extends CodedException {

  private final UUID id;

  public FailedTestCaseNotFoundException(UUID id) {
    super(
        SubmissionErrorCode.FAILED_TEST_CASE_NOT_FOUND,
        "Failed test case with id %s not found".formatted(id));
    this.id = id;
  }
}
