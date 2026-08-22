package com.codeclash.problemservice.testcase.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TestCaseNotFoundException extends CodedException {

  private final UUID id;

  public TestCaseNotFoundException(UUID id) {
    super(TestCaseErrorCode.TEST_CASE_NOT_FOUND, "Test case with id %s not found".formatted(id));
    this.id = id;
  }
}
