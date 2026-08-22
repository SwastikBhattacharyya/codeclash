package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidTestCaseIdException extends CodedException {

  private final UUID id;

  public InvalidTestCaseIdException(UUID id) {
    super(TestCaseErrorCode.INVALID_TEST_CASE_ID, "%s is not a valid test case id".formatted(id));
    this.id = id;
  }
}
